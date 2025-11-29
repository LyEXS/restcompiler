#include <stdio.h>
#include <unistd.h> // pour sleep

int main() {
    for(int i = 1; i <= 10; i++) { // 10 itérations seulement
        printf("Iteration %d\n", i);
        fflush(stdout); // s'assure que l'output est affiché immédiatement
        sleep(3);       // pause de 3 secondes par itération
    }
    return 0;
}