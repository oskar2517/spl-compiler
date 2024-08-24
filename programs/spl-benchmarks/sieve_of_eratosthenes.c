#include <stdio.h>

#define ARRAY_SIZE 999999

typedef int PrimesArray[ARRAY_SIZE];

void sieveOfEratosthenes(int n, PrimesArray primes) {
    int i, p, j;

    for (i = 0; i < ARRAY_SIZE; i++) {
        primes[i] = 1;
    }

    p = 2;
    while (p * p <= n) {
        if (primes[p] == 1) {
            for (j = p * p; j <= n; j += p) {
                primes[j] = 0;
            }
        }
        p++;
    }
}

int main() {
    PrimesArray primes;

    sieveOfEratosthenes(999998, primes);

    return 0;
}
