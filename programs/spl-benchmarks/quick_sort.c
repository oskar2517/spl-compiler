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

void swap(SortableArray arr, int i, int j) {
    int t = arr[i];
    arr[i] = arr[j];
    arr[j] = t;
}

void partition(SortableArray arr, int low, int high, int *result) {
    int pivot = arr[high];
    int i = low - 1;

    for (int j = low; j <= high - 1; j++) {
        if (arr[j] < pivot) {
            i++;
            swap(arr, i, j);
        }
    }

    swap(arr, i + 1, high);
    *result = i + 1;
}

void quickSort(SortableArray arr, int low, int high) {
    if (low < high) {
        int p;
        partition(arr, low, high, &p);

        quickSort(arr, low, p - 1);
        quickSort(arr, p + 1, high);
    }
}

int main() {
    SortableArray arr;
    int last = 0;

    for (int i = 0; i < ARRAY_SIZE; i++) {
        pseudoRandom(&last);
        arr[i] = last;
    }

    quickSort(arr, 0, ARRAY_SIZE - 1);

    return 0;
}
