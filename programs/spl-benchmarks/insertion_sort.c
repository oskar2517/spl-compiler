#include <stdio.h>

#define ARRAY_SIZE 99999

typedef int SortableArray[ARRAY_SIZE];

void pseudoRandom(int *last) {
    const int a = 1103515245;
    const int c = 12345;
    const int m = 2147483647;

    int product = a * (*last) + c;
    *last = product - (product / m) * m;
}

void insertionSort(SortableArray arr, int n) {
    int i, j, key;
    int condition;

    for (i = 1; i < n; i++) {
        key = arr[i];
        j = i - 1;

        condition = 1;
        if (j >= 0) {
            if (arr[j] > key) {
                condition = 1;
            } else {
                condition = 0;
            }
        } else {
            condition = 0;
        }

        while (condition == 1) {
            arr[j + 1] = arr[j];
            j = j - 1;

            if (j >= 0) {
                if (arr[j] > key) {
                    condition = 1;
                } else {
                    condition = 0;
                }
            } else {
                condition = 0;
            }
        }

        arr[j + 1] = key;
    }
}

int main() {
    SortableArray arr;
    int last = 0;
    int i;

    for (i = 0; i < ARRAY_SIZE; i++) {
        pseudoRandom(&last);
        arr[i] = last;
    }

    insertionSort(arr, ARRAY_SIZE);

    return 0;
}
