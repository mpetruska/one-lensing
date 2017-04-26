One-lensing data abstraction mini-library
=========================================

In most applications the "domain" logic is usually implemented with the help of
the data structures and values that describe the given problems domain. Unfortunately
there are cases when the developer does not have full control of the data types
used to model the domain (e.g., generated code, persistence library requirements).

If there's such restriction in place developers usually have two options on how to
implement the application logic:

1. Work directly with the types that adhere to the constraints given (e.g. use
   generated code).
   
2. Create the data types that support the application logic better, and also maintain
   mapping code that translates between the set of types that fulfill the constraints.

In order to reach to a third option, we need to think about this:  
Application logic does not need to explicitly restrict the data structures it works with;
it only needs to be able to access the atomic values that the structure holds. Here "access"
means the ability to extract the value and update it. Lenses naturally lend themselves to
solve this problem; hence the third option:

Write application logic in a way that it accesses the data values through Lenses.

This option has the following benefits:
- allows re-shaping data structures
- less boilerplate in most cases
- ...

Example
-------

...
