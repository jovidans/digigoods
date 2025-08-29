You are an expert software developer specializing in Java, Spring Boot, and unit testing with JUnit 5 and Mockito.

Your task is to create a comprehensive unit test class for the ReviewService class, specifically focusing on testing the createReview method.

**Requirements:**
1. **Test Framework:** Use JUnit 5 and Mockito for mocking dependencies
2. **Test Class Name:** Create a test class named `ReviewServiceTest`
3. **Coverage Goals:** Achieve comprehensive test coverage of the createReview method including:
  - Primary success path (happy path)
  - All potential failure scenarios and exception cases
  - Edge cases and boundary conditions
  - Input validation scenarios

**Mandatory Guidelines:**
- Strictly follow ALL guidelines, coding standards, and best practices detailed in `./.augment-guidelines`
- Use JUnit 5 conventions (not JUnit 4)
- Follow the Arrange-Act-Assert (AAA) pattern with clear one-liner comments
- Use Given-When-Then format for test method names
- Add `@DisplayName` annotations to all test methods with descriptive names
- Use Lombok where appropriate to reduce boilerplate code

**Process:**
1. First, analyze the ReviewService.java file and its createReview method to understand:
  - Method signature and parameters
  - Dependencies and collaborating classes
  - Business logic and validation rules
  - Exception handling scenarios
  - Return types and expected outcomes

2. Then create comprehensive unit tests that mock all external dependencies and test the method in isolation

3. Ensure tests cover both positive and negative scenarios, including but not limited to:
  - Valid input scenarios
  - Invalid input validation
  - Dependency failures
  - Business rule violations
  - Exception handling

**Deliverable:** A complete ReviewServiceTest.java file with comprehensive test coverage following all specified guidelines and best practices.
