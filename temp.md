[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[55,41] cannot find symbol
  symbol:   class DynamicPropertyRegistry
  location: class com.paymentgateway.common.testcontainers.KafkaTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[70,44] cannot find symbol
  symbol:   class DynamicPropertyRegistry
  location: class com.paymentgateway.common.testcontainers.PostgresTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[58,41] cannot find symbol
  symbol:   class DynamicPropertyRegistry
  location: class com.paymentgateway.common.testcontainers.RedisTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[54,6] cannot find symbol
  symbol:   class DynamicPropertySource
  location: class com.paymentgateway.common.testcontainers.KafkaTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[69,6] cannot find symbol
  symbol:   class DynamicPropertySource
  location: class com.paymentgateway.common.testcontainers.PostgresTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[57,6] cannot find symbol
  symbol:   class DynamicPropertySource
  location: class com.paymentgateway.common.testcontainers.RedisTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/wiremock/WireMockSupport.java:[126,48] cannot find symbol
  symbol:   method willSetStateTo(java.lang.String)
  location: variable mappingBuilder of type com.github.tomakehurst.wiremock.client.MappingBuilder
[INFO] 13 errors
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Payment Gateway - Common Observability 1.0.0-SNAPSHOT:
[INFO]
[INFO] Payment Gateway - Common Observability ............. SUCCESS [  3.565 s]
[INFO] Payment Gateway - Common Test Support .............. FAILURE [  0.940 s]
[INFO] Payment Gateway - Merchant Service ................. SKIPPED
[INFO] Payment Gateway - Token Vault Service .............. SKIPPED
[INFO] Payment Gateway - Payment Orchestrator Service ..... SKIPPED
[INFO] Payment Gateway - Acquiring Adapter Service ........ SKIPPED
[INFO] Payment Gateway - Provider Simulator ............... SKIPPED
[INFO] Payment Gateway - Webhook Service .................. SKIPPED
[INFO] Payment Gateway - Settlement Service ............... SKIPPED
[INFO] Payment Gateway - API Gateway ...................... SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.886 s
[INFO] Finished at: 2026-08-09T21:24:03+05:30
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.0:compile (default-compile) on project common-test-support: Compilation failure: Compilation failure:
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[55,41] cannot find symbol
[ERROR]   symbol:   class DynamicPropertyRegistry
[ERROR]   location: class com.paymentgateway.common.testcontainers.KafkaTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[70,44] cannot find symbol
[ERROR]   symbol:   class DynamicPropertyRegistry
[ERROR]   location: class com.paymentgateway.common.testcontainers.PostgresTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[3,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[4,40] package org.springframework.test.context does not exist
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[58,41] cannot find symbol
[ERROR]   symbol:   class DynamicPropertyRegistry
[ERROR]   location: class com.paymentgateway.common.testcontainers.RedisTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java:[54,6] cannot find symbol
[ERROR]   symbol:   class DynamicPropertySource
[ERROR]   location: class com.paymentgateway.common.testcontainers.KafkaTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java:[69,6] cannot find symbol
[ERROR]   symbol:   class DynamicPropertySource
[ERROR]   location: class com.paymentgateway.common.testcontainers.PostgresTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java:[57,6] cannot find symbol
[ERROR]   symbol:   class DynamicPropertySource
[ERROR]   location: class com.paymentgateway.common.testcontainers.RedisTestContainerBase
[ERROR] /D:/Distributed-Payment-Gateway-Platform/Distributed-Payment-Gateway-System/platform/common-test-support/src/main/java/com/paymentgateway/common/wiremock/WireMockSupport.java:[126,48] cannot find symbol
[ERROR]   symbol:   method willSetStateTo(java.lang.String)
[ERROR]   location: variable mappingBuilder of type com.github.tomakehurst.wiremock.client.MappingBuilder
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <args> -rf :common-test-support
