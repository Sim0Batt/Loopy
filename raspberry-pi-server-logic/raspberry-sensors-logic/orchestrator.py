import sys
import json
import subprocess
from datetime import datetime

import numpy as np
import heartpy as hp

from config import TEMP_BIN, PPG_BIN, GYRO_BIN


def run_bin(path, timeout):
    result = subprocess.run(
        [path],
        capture_output=True,
        text=True,
        timeout=timeout,
    )
    if result.returncode != 0:
        raise RuntimeError(
            "{} exited with code {}: {}".format(path, result.returncode, result.stderr.strip())
        )
    return json.loads(result.stdout)


def read_temperature():
    try:
        data = run_bin(TEMP_BIN, 10)
        return float(data["temperature"])
    except Exception as error:
        print("Temperature read failed: {}".format(error), file=sys.stderr)
        return 0.0


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
        data = run_bin(PPG_BIN, 15)
        bpm = compute_bpm(data["ir"], data["sample_rate"])
        spo2 = compute_spo2(data["red"], data["ir"])
        return {"heartRate": int(bpm), "oxygen": float(spo2)}
    except (FileNotFoundError, PermissionError):
        raise
    except Exception as error:
        print("PPG analysis failed: {}".format(error), file=sys.stderr)
        return {"heartRate": 0, "oxygen": 0.0}


def read_sweat():
    return {"sweating": 0.0}


def read_accelerometer():
    try:
        data = run_bin(GYRO_BIN, 15)
        acc_x = float(np.mean(data["acc_x"]))
        acc_y = float(np.mean(data["acc_y"]))
        acc_z = float(np.mean(data["acc_z"]))
        return {"acc_x": acc_x, "acc_y": acc_y, "acc_z": acc_z}
    except Exception as error:
        print("Accelerometer read failed: {}".format(error), file=sys.stderr)
        return {"acc_x": 0.0, "acc_y": 0.0, "acc_z": 0.0}


def main():
    timestamp = datetime.now().isoformat(timespec="seconds")
    temperature = read_temperature()
    ppg = read_ppg()
    sweat = read_sweat()
    acc = read_accelerometer()

    payload = {
        "heartRate": ppg["heartRate"],
        "oxygen": ppg["oxygen"],
        "timestampPPG": timestamp,
        "sweating": sweat["sweating"],
        "timestampElectrodes": timestamp,
        "temperature": temperature,
        "timestampTermometer": timestamp,
        "acc_x": acc["acc_x"],
        "acc_y": acc["acc_y"],
        "acc_z": acc["acc_z"],
        "timestampAccelerometer": timestamp,
    }

    print(json.dumps(payload, indent=4))


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("Orchestrator error: {}".format(error), file=sys.stderr)
        sys.exit(1)
    sys.exit(0)
