#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>

#define I2C_BUS             "/dev/i2c-1"
#define BNO055_ADDR         0x28

#define REG_CHIP_ID         0x00
#define REG_OPR_MODE        0x3D
#define REG_SYS_TRIGGER     0x3F
#define REG_ACC_X_LSB       0x08
#define REG_GYR_X_LSB       0x14
#define REG_LIA_X_LSB       0x28

#define CHIP_ID_BNO055      0xA0

#define OPR_MODE_CONFIG     0x00
#define OPR_MODE_NDOF       0x0C

#define ACC_SCALE           100.0
#define GYR_SCALE           16.0
#define LIA_SCALE           100.0

#define SAMPLE_COUNT        100
#define SAMPLE_INTERVAL_US  10000

static int write_reg(int fd, uint8_t reg, uint8_t value)
{
    uint8_t buffer[2];
    buffer[0] = reg;
    buffer[1] = value;
    if (write(fd, buffer, 2) != 2) {
        return -1;
    }
    return 0;
}

static int read_reg(int fd, uint8_t reg, uint8_t *value)
{
    if (write(fd, &reg, 1) != 1) {
        return -1;
    }
    if (read(fd, value, 1) != 1) {
        return -1;
    }
    return 0;
}

static int read_bytes(int fd, uint8_t reg, uint8_t *buf, int len)
{
    if (write(fd, &reg, 1) != 1) {
        return -1;
    }
    if (read(fd, buf, len) != len) {
        return -1;
    }
    return 0;
}

static int read_vector(int fd, uint8_t reg, int16_t *x, int16_t *y, int16_t *z)
{
    uint8_t buf[6];
    if (read_bytes(fd, reg, buf, 6) < 0) {
        return -1;
    }
    *x = (int16_t)(((uint16_t)buf[1] << 8) | buf[0]);
    *y = (int16_t)(((uint16_t)buf[3] << 8) | buf[2]);
    *z = (int16_t)(((uint16_t)buf[5] << 8) | buf[4]);
    return 0;
}

int main(void)
{
    int fd;
    int i;
    uint8_t chip_id;
    int16_t ax, ay, az;
    int16_t gx, gy, gz;
    int16_t lx, ly, lz;
    double acc_x[SAMPLE_COUNT], acc_y[SAMPLE_COUNT], acc_z[SAMPLE_COUNT];
    double gyr_x[SAMPLE_COUNT], gyr_y[SAMPLE_COUNT], gyr_z[SAMPLE_COUNT];
    double lia_x[SAMPLE_COUNT], lia_y[SAMPLE_COUNT], lia_z[SAMPLE_COUNT];

    fd = open(I2C_BUS, O_RDWR);
    if (fd < 0) {
        fprintf(stderr, "Failed to open bus %s: %s\n", I2C_BUS, strerror(errno));
        return 1;
    }

    if (ioctl(fd, I2C_SLAVE, BNO055_ADDR) < 0) {
        fprintf(stderr, "Failed to select slave 0x%02x: %s\n", BNO055_ADDR, strerror(errno));
        close(fd);
        return 1;
    }

    if (read_reg(fd, REG_CHIP_ID, &chip_id) < 0) {
        fprintf(stderr, "Failed to read chip id: %s\n", strerror(errno));
        close(fd);
        return 1;
    }

    if (chip_id != CHIP_ID_BNO055) {
        fprintf(stderr, "Unexpected chip id 0x%02x\n", chip_id);
        close(fd);
        return 1;
    }

    if (write_reg(fd, REG_OPR_MODE, OPR_MODE_CONFIG) < 0) {
        fprintf(stderr, "Failed to set config mode: %s\n", strerror(errno));
        close(fd);
        return 2;
    }
    usleep(25000);

    if (write_reg(fd, REG_SYS_TRIGGER, 0x20) < 0) {
        fprintf(stderr, "Failed to reset BNO055: %s\n", strerror(errno));
        close(fd);
        return 2;
    }
    usleep(650000);

    if (write_reg(fd, REG_OPR_MODE, OPR_MODE_NDOF) < 0) {
        fprintf(stderr, "Failed to set NDOF mode: %s\n", strerror(errno));
        close(fd);
        return 2;
    }
    usleep(20000);

    for (i = 0; i < SAMPLE_COUNT; i++) {
        if (read_vector(fd, REG_ACC_X_LSB, &ax, &ay, &az) < 0) {
            fprintf(stderr, "Failed to read accelerometer: %s\n", strerror(errno));
            close(fd);
            return 3;
        }
        if (read_vector(fd, REG_GYR_X_LSB, &gx, &gy, &gz) < 0) {
            fprintf(stderr, "Failed to read gyroscope: %s\n", strerror(errno));
            close(fd);
            return 3;
        }
        if (read_vector(fd, REG_LIA_X_LSB, &lx, &ly, &lz) < 0) {
            fprintf(stderr, "Failed to read linear acceleration: %s\n", strerror(errno));
            close(fd);
            return 3;
        }

        acc_x[i] = ax / ACC_SCALE;
        acc_y[i] = ay / ACC_SCALE;
        acc_z[i] = az / ACC_SCALE;
        gyr_x[i] = gx / GYR_SCALE;
        gyr_y[i] = gy / GYR_SCALE;
        gyr_z[i] = gz / GYR_SCALE;
        lia_x[i] = lx / LIA_SCALE;
        lia_y[i] = ly / LIA_SCALE;
        lia_z[i] = lz / LIA_SCALE;

        usleep(SAMPLE_INTERVAL_US);
    }

    close(fd);

    printf("{\"sample_count\": %d, ", SAMPLE_COUNT);
    printf("\"acc_x\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", acc_x[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"acc_y\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", acc_y[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"acc_z\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", acc_z[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"gyr_x\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", gyr_x[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"gyr_y\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", gyr_y[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"gyr_z\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", gyr_z[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"lia_x\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", lia_x[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"lia_y\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", lia_y[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("], \"lia_z\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%.4f", lia_z[i]);
        if (i < SAMPLE_COUNT - 1) printf(", ");
    }
    printf("]}\n");

    return 0;
}
