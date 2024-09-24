#include <stdio.h>
#include <pthread.h>
#include <errno.h>
#include <unistd.h>

#define handle_error_en(en, msg) \
do { errno = en; perror(msg); exit(EXIT_FAILURE); } while (0)

static int done = 0;
static int cleanup_pop_arg = 0;
static int cnt = 0;

void *threadMessage(void *message) {
    char threadName[50];
    sprintf(threadName, "thread %d", getpid());
    printf("%s: %s\n", threadName, (char*) message);

    return (void*) 42;
}

static void cleanup_handler(void *arg) {
    printf("%s\n", "Cleaning up");
    cnt = 0;
}

static void *thread_start(void *args) {
    pthread_cleanup_push(cleanup_handler, NULL);
    char *importantInfo[] = {"Mares eat oats", "Does eat oats", "Little lambs eat ivy", "A kid will eat ivy too"};

    time_t curr;
    curr = time(NULL);

    while (!done) {
        for (int i = 0; i < 4; ++i) {
            sleep(4);
            threadMessage(importantInfo[i]);
        }



        pthread_testcancel();
        if(curr < time(NULL)) {
            curr = time(NULL);
            printf("cnt = %d\n", cnt);
            cnt++;
        }
    }

    pthread_cleanup_pop(cleanup_pop_arg);
    return NULL;
}

int main(int argc, char *argv[]) {
    long patience = 0;
    int status;


    if(argc > 0) {
        if()
    }

    threadMessage("Starting MessageLoop Thread");
    pthread_t thread;
    status = pthread_create(&thread, NULL, thread_start, NULL);

    threadMessage("Waiting for MessageLoop thread to finish");

    sleep(patience);
    if


    return 0;
}
