# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**

There are two approaches used in this project.

For the Warehouse API, we are using an OpenAPI YAML file and generating code from it. The main advantage of this approach is that the API contract is clearly defined first. It makes the API more consistent, easier to document, and easier for frontend or other teams to understand. It also reduces manual mistakes because request/response models and interfaces can be generated automatically.

The disadvantage is that it adds some extra setup and maintenance. Whenever we need to change the API, we must update the YAML file and regenerate the code. Sometimes generated code can also be less flexible and harder to customize.

For Product and Store APIs, we coded the endpoints directly. The advantage of this approach is that it is faster and simpler for small APIs. Developers can directly write the controller/resource classes and make changes quickly without maintaining a separate YAML file.

The disadvantage is that documentation and API contract may become less consistent. If multiple developers work on the project, endpoint structure, response codes, and validation may differ. It can also create confusion for consumers if the API behavior is not documented properly.

In my opinion, for a professional project, I would prefer the OpenAPI-first approach, especially when the API is used by other teams or external clients. It gives a clear contract, better documentation, and consistency. However, for very small internal APIs or quick prototypes, direct coding is acceptable.

So, I would choose OpenAPI-first for important and shared APIs, and direct coding only for simple or internal endpoints.

```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
For this project, I would prioritize testing based on business importance and risk.

First, I would focus on unit tests for the core business logic, especially Warehouse creation, replacement, archive, validation rules, capacity checks, stock checks, and duplicate business unit code checks. These tests are fast, easy to run, and help catch logic issues early.

Second, I would write integration tests for REST APIs such as Warehouse, Product, and Store endpoints. These tests should verify request/response status codes like 200, 201, 204, 400, and 404, and also check database interaction. This is important because the project uses repositories and REST endpoints together.

Third, I would use parameterized tests for validation scenarios. For example, invalid capacity, stock greater than capacity, missing businessUnitCode, invalid location, or duplicate warehouse data. This helps test many cases with less duplicate code.

I would also add some negative test cases because they are important in real applications. For example, getting a warehouse that does not exist, deleting an invalid ID, or creating a warehouse with wrong input.

Due to time constraints, I would not try to test every getter, setter, or simple generated code. I would focus more on business rules, API behavior, error handling, and database flow.

To keep test coverage effective over time, I would run tests in the build pipeline, use JaCoCo coverage reports, and maintain a minimum coverage rule. But I would not focus only on percentage. Good coverage should mean important business scenarios are tested properly.

So, my testing priority would be:
1. Unit tests for business logic
2. Integration tests for APIs and database flow
3. Parameterized tests for validations
4. Negative tests for error scenarios
5. Coverage monitoring using JaCoCo

This approach gives a good balance between quality, time, and maintainability.
```
