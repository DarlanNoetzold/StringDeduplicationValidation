#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define TABLE_SIZE 10007

typedef struct Entry {
    char *key;
    struct Entry *next;
} Entry;

typedef struct {
    Entry **entries;
} HashMap;

HashMap* createHashMap() {
    HashMap *hashMap = (HashMap *) malloc(sizeof(HashMap));
    if (!hashMap) {
        fprintf(stderr, "Failed to allocate memory for HashMap\n");
        return NULL;
    }
    hashMap->entries = (Entry **) malloc(TABLE_SIZE * sizeof(Entry *));
    if (!hashMap->entries) {
        fprintf(stderr, "Failed to allocate memory for HashMap entries\n");
        free(hashMap);
        return NULL;
    }
    for (int i = 0; i < TABLE_SIZE; i++) {
        hashMap->entries[i] = NULL;
    }
    return hashMap;
}

unsigned int hash(const char *key) {
    unsigned int hashValue = 0;
    for (int i = 0; key[i] != '\0'; i++) {
        hashValue = (hashValue << 5) + key[i];
    }
    return hashValue % TABLE_SIZE;
}

void insert(HashMap *hashMap, const char *key) {
    unsigned int slot = hash(key);
    Entry *entry = hashMap->entries[slot];
    while (entry != NULL) {
        if (strcmp(entry->key, key) == 0) {
            return; // Key already exists, no need to insert
        }
        entry = entry->next;
    }
    entry = (Entry *) malloc(sizeof(Entry));
    if (!entry) {
        fprintf(stderr, "Failed to allocate memory for Entry\n");
        return;
    }
    entry->key = strdup(key);
    if (!entry->key) {
        fprintf(stderr, "Failed to duplicate string\n");
        free(entry);
        return;
    }
    entry->next = hashMap->entries[slot];
    hashMap->entries[slot] = entry;
}

int contains(HashMap *hashMap, const char *key) {
    unsigned int slot = hash(key);
    Entry *entry = hashMap->entries[slot];
    while (entry != NULL) {
        if (strcmp(entry->key, key) == 0) {
            return 1; // Key found
        }
        entry = entry->next;
    }
    return 0; // Key not found
}

void deduplicateStrings(char **strings, int length, int *inspectedCount, int *deduplicatedCount, size_t *deduplicatedSize) {
    HashMap *hashMap = createHashMap();
    if (!hashMap) {
        fprintf(stderr, "Failed to create HashMap\n");
        return;
    }
    *inspectedCount = 0;
    *deduplicatedCount = 0;
    *deduplicatedSize = 0;

    for (int i = 0; i < length; i++) {
        (*inspectedCount)++;
        if (!contains(hashMap, strings[i])) {
            insert(hashMap, strings[i]);
            (*deduplicatedSize) += strlen(strings[i]);
        } else {
            free(strings[i]);
            strings[i] = NULL;
            (*deduplicatedCount)++;
        }
    }
}

JNIEXPORT void JNICALL Java_OptimizedStringDeduplication_deduplicateStrings
  (JNIEnv *env, jclass clazz, jobjectArray jStrings) {
    int length = (*env)->GetArrayLength(env, jStrings);
    char **strings = (char **) malloc(length * sizeof(char *));
    if (!strings) {
        fprintf(stderr, "Failed to allocate memory for strings array\n");
        return;
    }
    for (int i = 0; i < length; i++) {
        jstring jStr = (jstring) (*env)->GetObjectArrayElement(env, jStrings, i);
        const char *str = (*env)->GetStringUTFChars(env, jStr, 0);
        strings[i] = strdup(str);
        (*env)->ReleaseStringUTFChars(env, jStr, str);
        if (!strings[i]) {
            fprintf(stderr, "Failed to duplicate string\n");
            // Free already allocated strings
            for (int j = 0; j < i; j++) {
                free(strings[j]);
            }
            free(strings);
            return;
        }
    }

    int inspectedCount, deduplicatedCount;
    size_t deduplicatedSize;
    clock_t start = clock();
    deduplicateStrings(strings, length, &inspectedCount, &deduplicatedCount, &deduplicatedSize);
    clock_t end = clock();
    double deduplicationTime = (double)(end - start) / CLOCKS_PER_SEC * 1000; // in milliseconds

    printf("Inspected Strings: %d\n", inspectedCount);
    printf("Deduplicated Strings: %d\n", deduplicatedCount);
    printf("Deduplicated Size: %zu bytes\n", deduplicatedSize);
    printf("Deduplicated Percentage: %.2f%%\n", (double)deduplicatedCount / inspectedCount * 100);
    printf("Deduplication Time: %.2f ms\n", deduplicationTime);

    fflush(stdout); // Ensure that the logs are flushed immediately

    for (int i = 0; i < length; i++) {
        if (strings[i] != NULL) {
            (*env)->SetObjectArrayElement(env, jStrings, i, (*env)->NewStringUTF(env, strings[i]));
        }
    }
    for (int i = 0; i < length; i++) {
        if (strings[i] != NULL) {
            free(strings[i]);
        }
    }
    free(strings);
}
