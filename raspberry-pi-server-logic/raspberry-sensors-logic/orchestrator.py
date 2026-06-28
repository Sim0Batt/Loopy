import sys
import json
import subprocess
from datetime import datetime

import numpy as np
import heartpy as hp
import requests

from config import USER_ID, SERVER_URL, TEMP_BIN, PPG_BIN


def read_temperature():
    result = subprocess.run(
        [TEMP_BIN],
        capture_output=True,
        text=True,
        timeout=10,
    )
    if result.returncode != 0:
        raise RuntimeError(
            "temp_bin exited with code {}: {}".format(
                result.returncode, result.stderr.strip()
            )
        )
    data = json.loads(result.stdout)
    return float(data["temperature"])


def compute_bpm(ir, sample_rate):
    signal = np.array(ir, dtype=float)
    rate = float(sample_rate)
    filtered = hp.filter_signal(
        signal,
        cutoff=[0.7, 3.5],
        sample_rate=rate,
        order=3,
        filtertype="bandpass",
    )
    working_data, measures = hp.process(filtered, rate, bpmmin=40, bpmmax=180)
    bpm = measures["bpm"]
    if bpm is None or np.isnan(bpm):
        raise ValueError("heartpy returned an invalid bpm")
    return bpm


def compute_spo2(red, ir):
    red_signal = np.array(red, dtype=float)
    ir_signal = np.array(ir, dtype=float)
    dc_red = np.mean(red_signal)
    dc_ir = np.mean(ir_signal)
    ac_red = np.std(red_signal)
    ac_ir = np.std(ir_signal)
    if dc_red == 0 or dc_ir == 0 or ac_ir == 0:
        raise ValueError("invalid PPG signal for SpO2")
    ratio = (ac_red / dc_red) / (ac_ir / dc_ir)
    spo2 = 110.0 - 25.0 * ratio
    if spo2 < 0.0:
        return 0.0
    if spo2 > 100.0:
        return 100.0
    return spo2


def read_ppg():
    try:
        result = subprocess.run(
            [PPG_BIN],
            capture_output=True,
            text=True,
            timeout=15,
        )
        if result.returncode != 0:
            raise RuntimeError(
                "ppg_bin exited with code {}: {}".format(
                    result.returncode, result.stderr.strip()
                )
            )
        data = json.loads(result.stdout)
        sample_rate = data["sample_rate"]
        ir = data["ir"]
        red = data["red"]
        bpm = compute_bpm(ir, sample_rate)
        spo2 = compute_spo2(red, ir)
        return {"heartRate": int(bpm), "oxygen": float(spo2)}
    except (FileNotFoundError, PermissionError):
        raise
    except Exception as error:
        print("PPG analysis failed: {}".format(error), file=sys.stderr)
        return {"heartRate": 0, "oxygen": 0.0}


def read_sweat():
    return {"sweating": 0.0}


def build_payload(temperature, ppg, sweat):
    timestamp = datetime.now().isoformat(timespec="seconds")
    return {
        "heartRate": int(ppg["heartRate"]),
        "oxygen": float(ppg["oxygen"]),
        "timestampPPG": timestamp,
        "sweating": float(sweat["sweating"]),
        "timestampElectrodes": timestamp,
        "temperature": float(temperature),
        "timestampTermometer": timestamp,
        "acc_x": 0.0,
        "acc_y": 0.0,
        "acc_z": 0.0,
        "timestampAccelerometer": timestamp,
    }


def send(payload):
    url = "{}/saveData/{}".format(SERVER_URL, USER_ID)
    response = requests.post(
        url,
        json=payload,
        headers={"Content-Type": "application/json"},
        timeout=10,
    )
    response.raise_for_status()


def main():
    try:
        temperature = read_temperature()
        ppg = read_ppg()
        sweat = read_sweat()
        payload = build_payload(temperature, ppg, sweat)
        send(payload)
    except Exception as error:
        print("Orchestrator error: {}".format(error), file=sys.stderr)
        sys.exit(1)
    sys.exit(0)


if __name__ == "__main__":
    main()
