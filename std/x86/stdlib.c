#include <stdio.h>
#include <stdlib.h>
#include <time.h>

time_t start_time;

void __init_time() {
    start_time = time(NULL);
}

void _time(long long *address) {
    *address = time(NULL) - start_time;
}

void _printi(long long i) {
    printf("%lld", i);
}

void _printc(long long c) {
    printf("%c", (char) c);
}

void _exit() {
    exit(0);
}

void _readi(long long *address) {
    scanf("%lld", address);
}

void _readc(long long *address) {
    scanf("%c", (char *) address);
}

#ifndef HEADLESS

#include <SDL2/SDL.h>

#define SCREEN_WIDTH 640
#define SCREEN_HEIGHT 480

#define COLOR_R(c) ((c) >> 16) & 0xFF
#define COLOR_G(c) ((c) >> 8) & 0xFF
#define COLOR_B(c) (c) & 0xFF

SDL_Window* window;
SDL_Renderer* renderer;

void __sdl_init_screen() {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("SDL could not initialize! SDL_Error: %s\n", SDL_GetError());
        exit(1);
    }

    window = SDL_CreateWindow("SDL Window", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, SCREEN_WIDTH, SCREEN_HEIGHT, SDL_WINDOW_SHOWN);
    if (window == NULL) {
        printf("Window could not be created! SDL_Error: %s\n", SDL_GetError());
         exit(1);
    }

    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (renderer == NULL) {
        printf("Renderer could not be created! SDL_Error: %s\n", SDL_GetError());
        exit(1);
    }

    SDL_SetRenderDrawColor(renderer, 255, 255, 255, SDL_ALPHA_OPAQUE);
    SDL_RenderClear(renderer);
}

void __sdl_event_loop() {
    SDL_RenderPresent(renderer);
    SDL_Event event;
    int quit = 0;
    while (!quit) {
        while (SDL_PollEvent(&event) != 0) {
            if (event.type == SDL_QUIT) {
                quit = 1;
            }
        }
    }

    SDL_DestroyWindow(window);
    SDL_Quit();
}

void _setPixel(long long x, long long y, long long color) {
    SDL_SetRenderDrawColor(renderer, COLOR_R(color), COLOR_G(color), COLOR_B(color), SDL_ALPHA_OPAQUE);
    SDL_RenderDrawPoint(renderer, x, y);
}

void _drawLine(long long x1, long long y1, long long x2, long long y2, long long color) {
    SDL_SetRenderDrawColor(renderer, COLOR_R(color), COLOR_G(color), COLOR_B(color), SDL_ALPHA_OPAQUE);
    SDL_RenderDrawLine(renderer, x1, y1, x2, y2);
}

void _drawCircle(long long x0, long long y0, long long radius, long long color) {
    const long long diameter = (radius * 2);

    long long x = (radius - 1);
    long long y = 0;
    long long tx = 1;
    long long ty = 1;
    long long error = (tx - diameter);

    SDL_SetRenderDrawColor(renderer, COLOR_R(color), COLOR_G(color), COLOR_B(color), SDL_ALPHA_OPAQUE);

    while (x >= y) {
        SDL_RenderDrawPoint(renderer, x0 + x, y0 - y);
        SDL_RenderDrawPoint(renderer, x0 + x, y0 + y);
        SDL_RenderDrawPoint(renderer, x0 - x, y0 - y);
        SDL_RenderDrawPoint(renderer, x0 - x, y0 + y);
        SDL_RenderDrawPoint(renderer, x0 + y, y0 - x);
        SDL_RenderDrawPoint(renderer, x0 + y, y0 + x);
        SDL_RenderDrawPoint(renderer, x0 - y, y0 - x);
        SDL_RenderDrawPoint(renderer, x0 - y, y0 + x);

        if (error <= 0) {
            ++y;
            error += ty;
            ty += 2;
        }

        if (error > 0) {
            --x;
            tx += 2;
            error += (tx - diameter);
        }
    }
}

void _clearAll(long long color) {
    SDL_SetRenderDrawColor(renderer, COLOR_R(color), COLOR_G(color), COLOR_B(color), SDL_ALPHA_OPAQUE);
    SDL_RenderClear(renderer);
}

#endif