## @Component 

is a Spring stereotype annotation that tells the Spring IoC (Inversion of Control) container to automatically detect, create, and manage an object (called a Bean). During application startup, Spring scans the configured packages, finds classes annotated with @Component, instantiates them, manages their lifecycle, and makes them available for Dependency Injection (@Autowired or constructor injection). It is the most generic stereotype annotation and is typically used for helper classes, utility components, mappers, validators, and other classes that don't specifically belong to the Service, Repository, or Controller layers.

# Think of @Bean as telling Spring:

"Spring, whenever the application starts, create this object for me and manage it throughout the application's lifetime."

Unlike @Component, where Spring creates the object by scanning a class, @Bean is used when you want to create the object yourself inside a configuration method.

Example
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

When Spring starts:

It finds the @Configuration class.
It executes the objectMapper() method.
It creates one ObjectMapper object.
It stores it in the Spring container.
Any class can inject it using constructor injection or @Autowired.
Why do we use @Bean?

Use @Bean when:

The class belongs to a third-party library (you can't annotate it with @Component).
You need custom initialization.
You want full control over how the object is created.
Difference between @Component and @Bean
@Component → Spring finds the class and creates the object automatically.
@Bean → You write the code to create the object, and Spring manages it afterward.
One-line note

@Bean tells Spring to execute a method during startup, create the returned object, register it as a Spring Bean, and make it available for Dependency Injection throughout the application.

# @Configuration tells Spring:

"This class contains the application's configuration and defines one or more Spring Beans."

Spring treats a @Configuration class as a blueprint for creating and configuring objects that should be managed by the Spring container.

Example
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

When the application starts:

Spring finds the @Configuration class.
It executes every method annotated with @Bean.
The returned objects are registered as Spring Beans.
Other classes can inject and use those beans.
Why do we use @Configuration?
To centralize application configuration.
To define reusable Spring Beans.
To configure third-party libraries (e.g., Kafka, Redis, ObjectMapper, RestTemplate, WebClient, Security).
One-line note

@Configuration marks a class as a Spring configuration class that contains Bean definitions. During application startup, Spring processes this class, creates the Beans defined inside it, and manages them in the IoC container for Dependency Injection.

Relationship
@Configuration
        │
        ├── @Bean
        ├── @Bean
        ├── @Bean
        └── @Bean

Think of it like this:

@Configuration = The factory (or blueprint) where you define objects.
@Bean = The individual object that the factory creates and hands over to Spring to manage.