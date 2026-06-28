#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>

#define I2C_BUS            "/dev/i2c-1"
#define MAX30102_ADDR      0x57

#define REG_FIFO_WR_PTR    0x04
#define REG_OVF_COUNTER    0x05
#define REG_FIFO_RD_PTR    0x06
#define REG_FIFO_DATA      0x07
#define REG_FIFO_CONFIG    0x08
#define REG_MODE_CONFIG    0x09
#define REG_SPO2_CONFIG    0x0A
#define REG_LED1_PA        0x0C
#define REG_LED2_PA        0x0D
#define REG_PART_ID        0xFF

#define MODE_RESET         0x40
#define MODE_SPO2          0x03
#define FIFO_CONFIG_VAL    0x10
#define SPO2_CONFIG_VAL    0x27
#define LED_PA_VAL         0x04
#define PART_ID_MAX30102   0x15

#define SAMPLE_RATE        100
#define SAMPLE_COUNT       400
#define FIFO_MASK          0x1F
#define BYTES_PER_SAMPLE   6

#define EXIT_INIT          1
#define EXIT_CONFIG        2
#define EXIT_READ          3

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

static int read_fifo(int fd, uint8_t *data, int length)
{
    uint8_t reg = REG_FIFO_DATA;
    if (write(fd, &reg, 1) != 1) {
        return -1;
    }
    if (read(fd, data, length) != length) {
        return -1;
    }
    return 0;
}

int main(void)
{
    int fd;
    int i;
    int count;
    int tries;
    uint8_t mode_value;
    uint8_t part_id;
    uint8_t write_ptr;
    uint8_t read_ptr;
    uint8_t sample[BYTES_PER_SAMPLE];
    uint32_t ir_buffer[SAMPLE_COUNT];
    uint32_t red_buffer[SAMPLE_COUNT];

    fd = open(I2C_BUS, O_RDWR);
    if (fd < 0) {
        fprintf(stderr, "Failed to open bus %s: %s\n", I2C_BUS, strerror(errno));
        return EXIT_INIT;
    }

    if (ioctl(fd, I2C_SLAVE, MAX30102_ADDR) < 0) {
        fprintf(stderr, "Failed to select slave 0x%02x: %s\n", MAX30102_ADDR, strerror(errno));
        close(fd);
        return EXIT_INIT;
    }

    if (write_reg(fd, REG_MODE_CONFIG, MODE_RESET) < 0) {
        fprintf(stderr, "Failed to reset MAX30102: %s\n", strerror(errno));
        close(fd);
        return EXIT_INIT;
    }

    tries = 0;
    mode_value = MODE_RESET;
    do {
        usleep(1000);
        if (read_reg(fd, REG_MODE_CONFIG, &mode_value) < 0) {
            fprintf(stderr, "Failed to read mode register: %s\n", strerror(errno));
            close(fd);
            return EXIT_INIT;
        }
        tries++;
    } while ((mode_value & MODE_RESET) && tries < 100);

    if (mode_value & MODE_RESET) {
        fprintf(stderr, "MAX30102 reset timeout\n");
        close(fd);
        return EXIT_INIT;
    }

    if (read_reg(fd, REG_PART_ID, &part_id) < 0) {
        fprintf(stderr, "Failed to read part id: %s\n", strerror(errno));
        close(fd);
        return EXIT_INIT;
    }

    if (part_id != PART_ID_MAX30102) {
        fprintf(stderr, "Unexpected part id 0x%02x\n", part_id);
        close(fd);
        return EXIT_INIT;
    }

    if (write_reg(fd, REG_FIFO_CONFIG, FIFO_CONFIG_VAL) < 0 ||
        write_reg(fd, REG_MODE_CONFIG, MODE_SPO2) < 0 ||
        write_reg(fd, REG_SPO2_CONFIG, SPO2_CONFIG_VAL) < 0 ||
        write_reg(fd, REG_LED1_PA, LED_PA_VAL) < 0 ||
        write_reg(fd, REG_LED2_PA, LED_PA_VAL) < 0) {
        fprintf(stderr, "Failed to configure MAX30102: %s\n", strerror(errno));
        close(fd);
        return EXIT_CONFIG;
    }

    if (write_reg(fd, REG_FIFO_WR_PTR, 0x00) < 0 ||
        write_reg(fd, REG_OVF_COUNTER, 0x00) < 0 ||
        write_reg(fd, REG_FIFO_RD_PTR, 0x00) < 0) {
        fprintf(stderr, "Failed to clear FIFO pointers: %s\n", strerror(errno));
        close(fd);
        return EXIT_CONFIG;
    }

    count = 0;
    while (count < SAMPLE_COUNT) {
        int available;

        if (read_reg(fd, REG_FIFO_WR_PTR, &write_ptr) < 0 ||
            read_reg(fd, REG_FIFO_RD_PTR, &read_ptr) < 0) {
            fprintf(stderr, "Failed to read FIFO pointers: %s\n", strerror(errno));
            close(fd);
            return EXIT_READ;
        }

        available = (write_ptr - read_ptr) & FIFO_MASK;
        if (available == 0) {
            usleep(5000);
            continue;
        }

        for (i = 0; i < available && count < SAMPLE_COUNT; i++) {
            if (read_fifo(fd, sample, BYTES_PER_SAMPLE) < 0) {
                fprintf(stderr, "Failed to read FIFO data: %s\n", strerror(errno));
                close(fd);
                return EXIT_READ;
            }

            red_buffer[count] = ((uint32_t)(sample[0] & 0x03) << 16) |
                                ((uint32_t)sample[1] << 8) |
                                (uint32_t)sample[2];
            ir_buffer[count] = ((uint32_t)(sample[3] & 0x03) << 16) |
                               ((uint32_t)sample[4] << 8) |
                               (uint32_t)sample[5];
            count++;
        }
    }

    close(fd);

    printf("{\"sample_rate\": %d, \"ir\": [", SAMPLE_RATE);
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%u", ir_buffer[i]);
        if (i < SAMPLE_COUNT - 1) {
            printf(", ");
        }
    }
    printf("], \"red\": [");
    for (i = 0; i < SAMPLE_COUNT; i++) {
        printf("%u", red_buffer[i]);
        if (i < SAMPLE_COUNT - 1) {
            printf(", ");
        }
    }
    printf("]}\n");

    return 0;
}
