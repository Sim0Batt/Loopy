#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>

#define I2C_BUS        "/dev/i2c-1"
#define MAX30205_ADDR  0x4c
#define TEMP_REG       0x00
#define TEMP_LSB       0.00390625

int main(void)
{
    int fd;
    uint8_t reg = TEMP_REG;
    uint8_t buf[2];
    int16_t raw;
    double temperature;

    fd = open(I2C_BUS, O_RDWR);
    if (fd < 0) {
        fprintf(stderr, "Failed to open bus %s: %s\n", I2C_BUS, strerror(errno));
        return 1;
    }

    if (ioctl(fd, I2C_SLAVE, MAX30205_ADDR) < 0) {
        fprintf(stderr, "Failed to select slave 0x%02x: %s\n", MAX30205_ADDR, strerror(errno));
        close(fd);
        return 2;
    }

    if (write(fd, &reg, 1) != 1) {
        fprintf(stderr, "Failed to write register 0x%02x: %s\n", TEMP_REG, strerror(errno));
        close(fd);
        return 3;
    }

    if (read(fd, buf, 2) != 2) {
        fprintf(stderr, "Failed to read temperature from MAX30205: %s\n", strerror(errno));
        close(fd);
        return 4;
    }

    close(fd);

    raw = (int16_t)(((uint16_t)buf[0] << 8) | buf[1]);
    temperature = raw * TEMP_LSB;

    printf("{\"temperature\": %.4f}\n", temperature);

    return 0;
}
