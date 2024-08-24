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

void bubbleSort(SortableArray arr, int n) {
    int i, j, t;
    int swapped;

    i = 0;
    while (i < n - 1) {
        swapped = 0;

        for (j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                t = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = t;
                swapped = 1;
            }
        }

        if (swapped == 0) {
            break; // Exit the loop early if no swaps occurred
        }

        i++;
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

    bubbleSort(arr, ARRAY_SIZE);

    return 0;
}