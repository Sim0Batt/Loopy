CC      = gcc
CFLAGS  = -Wall -Wextra -O2

all: temp_bin ppg_bin

temp_bin: temp_sensor.c
	$(CC) $(CFLAGS) -o temp_bin temp_sensor.c

ppg_bin: ppg_sensor.c
	$(CC) $(CFLAGS) -o ppg_bin ppg_sensor.c

clean:
	rm -f temp_bin ppg_bin

.PHONY: all clean
