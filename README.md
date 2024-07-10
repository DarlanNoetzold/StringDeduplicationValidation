# String Deduplication Validation

## Overview
This project aims to investigate the efficiency of the String Deduplication parameter in different versions of the Java Virtual Machine (JVM). We developed a web crawler application using Spring Boot to explore large hierarchical websites such as Wikipedia. The analysis focuses on the performance and memory usage with and without the String Deduplication parameter across JVM versions 11, 17, and 21.

## Repository Structure
The repository is organized into several subdirectories, each corresponding to different JVM versions and the enhancement for Java 21:

- [App in Java 11](https://github.com/DarlanNoetzold/StringDeduplicationValidation/tree/main/App%20in%20Java%2011)
- [App in Java 17](https://github.com/DarlanNoetzold/StringDeduplicationValidation/tree/main/App%20in%20Java%2017)
- [App in Java 21](https://github.com/DarlanNoetzold/StringDeduplicationValidation/tree/main/App%20in%20Java%2021)
- [Improve String Deduplication in Java 21](https://github.com/DarlanNoetzold/StringDeduplicationValidation/tree/main/Emprove%20StringDeduplication%20in%20Java%2021)

## Crawlers
Each subdirectory contains the web crawler application configured for the respective JVM version. The web crawler is designed to extensively explore Wikipedia, ensuring a high probability of string duplication, which is essential for analyzing the efficiency of the String Deduplication parameter.

## Enhancement for Java 21
The `Improve String Deduplication in Java 21` directory contains enhancements aimed at improving the String Deduplication mechanism in JVM version 21. This involves a custom deduplication algorithm implemented in C, which is integrated with the Java application.

## Test Results
The test results for String Deduplication across different JVM versions are documented in the following GC reports:

- [Java 11 GC report](https://rb.gy/8o440d)
- [Java 17 GC report](https://rb.gy/2hoz1p)
- [Java 21 GC report](https://rb.gy/8n74lu)

### Performance Metrics
The performance metrics, including CPU and memory usage with and without String Deduplication, are illustrated in the images below:

#### Java 11
- [CPU Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_11.png)
- [CPU Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_11_sem_SD.png)
- [Memory Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_11.png)
- [Memory Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_11_sem_SD.png)

#### Java 17
- [CPU Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_17.png)
- [CPU Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_17_sem_SD.png)
- [Memory Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_17.png)
- [Memory Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_17_sem_SD.png)

#### Java 21
- [CPU Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_22.png)
- [CPU Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_cpu_22_sem_SD.png)
- [Memory Usage](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_22.png)
- [Memory Usage without String Deduplication](https://github.com/DarlanNoetzold/StringDeduplicationValidation/blob/main/java_memory_22_sem_SD.png)

## Custom Deduplication Solution
The custom deduplication algorithm implemented in C for Java 21 significantly improves the performance of String Deduplication. The algorithm utilizes a hash map to track and remove duplicate strings efficiently.

### Integration with Java
To integrate the C algorithm with the Java application, the native method `deduplicateStrings` is called within the Java code. This method processes the strings and outputs detailed logs of the deduplication process, which are captured and displayed by the Java application.

### Running the Application
To run the application and validate the custom deduplication solution, follow these steps:

1. **Compile the C code:**
   gcc -shared -o libdeduplication.so -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux deduplication.c

2. **Place the compiled library in the appropriate directory:**
   mv libdeduplication.so /path/to/your/library/directory

3. **Set the `java.library.path` to the directory containing the library:**
   java -Djava.library.path=/path/to/your/library/directory -jar your-application.jar

4. **Run the application:**
   ./mvnw spring-boot:run

For more detailed instructions, refer to the [Improve String Deduplication in Java 21](https://github.com/DarlanNoetzold/StringDeduplicationValidation/tree/main/Emprove%20StringDeduplication%20in%20Java%2021) directory.

## Conclusion
The results indicate that while String Deduplication offers memory savings, its efficiency diminishes in newer JVM versions. The custom solution for Java 21 demonstrates potential improvements, offering valuable insights for developers and engineers in optimizing JVM performance.
