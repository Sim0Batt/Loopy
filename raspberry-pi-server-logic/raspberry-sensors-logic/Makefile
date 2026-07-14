CC      = gcc
CFLAGS  = -Wall -Wextra -O2

all: temp_bin ppg_bin gyro_bin

temp_bin: temp_sensor.c
	$(CC) $(CFLAGS) -o temp_bin temp_sensor.c

ppg_bin: ppg_sensor.c
	$(CC) $(CFLAGS) -o ppg_bin ppg_sensor.c

gyro_bin: gyroscope.c
	$(CC) $(CFLAGS) -o gyro_bin gyroscope.c

clean:
	rm -f temp_bin ppg_bin gyro_bin

.PHONY: all clean
