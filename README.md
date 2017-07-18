# Zero-Dependency Integration Testing Using Traits

The code in this repository shows a stepwise evolution of a small codebase which has an external runtime dependency on a service; by making use of design patterns, we can make our client code far more robust and shorten our development cycle for the inevitable breaking changes in the external services.

## Roadmap

  The code in the `service-1.x` subprojects is not a model of software engineering; rather, it is intended to represent an external service over which you, the developer, have no direct control and whose behavior can at times be less than ideal.

  The `biblio-client` subproject is the application code that you, the developer, is responsible for.
  
  The four branches of the project are covered in the talk in the following order:

  1. `InlineExternalCalls`: As its name suggests, all calls to the external service are inlined into the body of the client class.
  2. `ExternalServiceConnector`: Seeing an opportunity to use the Single Responsiblity Principle, our developer segregates talking to the external service from providing application logic to a consumer. However, the external service code is still tightly coupled to the application logic.
  3. `ServiceTrait`: The heart of this presentation, where instead of hard-coding the behavior of the client to a particular implementation, we make use of the Bridge Pattern to separate a behavior contract (an interface) from the particular implementation; this allows us to test our application logic in isolation, and perform unit and integration tests as we see fit.
  4. `SwapOutExternalService`: Finally, the day comes when we have to point our client at a different service entirely. If we have followed these patterns, we will experience a minimum of pain and suffering finding all the locations in the code where specifics of the external service need to be changed.