# Template Guide

Learn how to create powerful templates using TFPP and Thymeleaf.

## Template Modes

TFPP automatically detects the appropriate template mode based on your file extension:

### HTML Templates
- **Extensions**: `.html`, `.htm`
- **Features**: Full Thymeleaf HTML processing, form handling, fragments
- **Use cases**: Web pages, email templates, reports

### XML Templates
- **Extensions**: `.xml`
- **Features**: XML-aware processing, namespace support
- **Use cases**: Configuration files, RSS feeds, SOAP messages

### Text Templates
- **Extensions**: `.txt`, `.text`, `.md`, `.properties`
- **Features**: Plain text processing, minimal markup
- **Use cases**: Configuration files, plain text emails, scripts

### CSS Templates
- **Extensions**: `.css`
- **Features**: CSS-aware processing, variable substitution
- **Use cases**: Dynamic stylesheets, themed CSS

### JavaScript Templates
- **Extensions**: `.js`
- **Features**: JavaScript-aware processing, safe variable injection
- **Use cases**: Dynamic JavaScript, configuration objects

## Basic Syntax

### Variable Expressions

The most common way to display data:

```html
<!-- Inline expressions -->
<p>Hello [[${name}]]!</p>
<p>Your score: [[${score}]]</p>

<!-- Attribute expressions -->
<div th:text="${message}">Default message</div>
<span th:text="${user.email}">user@example.com</span>
```

### Conditional Processing

Display content based on conditions:

```html
<!-- Simple conditions -->
<div th:if="${user.active}">Welcome back!</div>
<div th:unless="${user.active}">Please activate your account</div>

<!-- Complex conditions -->
<div th:if="${user.age >= 18}">
    <p>You can access all features</p>
</div>

<!-- Switch-like conditionals -->
<div th:switch="${user.role}">
    <p th:case="'admin'">You have admin privileges</p>
    <p th:case="'user'">You have standard access</p>
    <p th:case="*">Unknown role</p>
</div>
```

### Iteration

Loop through collections:

```html
<!-- Simple list iteration -->
<ul>
    <li th:each="item : ${items}" th:text="${item.name}">Item name</li>
</ul>

<!-- Iteration with status -->
<table>
    <tr th:each="product,iterStat : ${products}">
        <td th:text="${iterStat.count}">1</td>
        <td th:text="${product.name}">Product name</td>
        <td th:text="${product.price}">Price</td>
    </tr>
</table>

<!-- Map iteration -->
<dl th:each="entry : ${settings}">
    <dt th:text="${entry.key}">Key</dt>
    <dd th:text="${entry.value}">Value</dd>
</dl>
```

## Advanced Features

### Expression Utility Objects

Thymeleaf provides built-in utilities:

```html
<!-- Date formatting -->
<p>Today: [[${#dates.format(today, 'yyyy-MM-dd')}]]</p>

<!-- String manipulation -->
<p>Upper: [[${#strings.toUpperCase(name)}]]</p>
<p>Length: [[${#strings.length(message)}]]</p>

<!-- Number formatting -->
<p>Price: [[${#numbers.formatDecimal(price, 1, 2)}]]</p>

<!-- Collection utilities -->
<p>Size: [[${#lists.size(items)}]]</p>
<p>Empty: [[${#lists.isEmpty(items)}]]</p>
```

### Fragment Processing

Reuse template parts:

```html
<!-- Define a fragment -->
<div th:fragment="userCard(user)">
    <div class="card">
        <h3 th:text="${user.name}">User Name</h3>
        <p th:text="${user.email}">user@example.com</p>
    </div>
</div>

<!-- Include the fragment -->
<div th:insert="fragments :: userCard(${currentUser})"></div>
```

### Local Variables

Create variables within templates:

```html
<div th:with="fullName=${user.firstName + ' ' + user.lastName}">
    <p th:text="${fullName}">Full Name</p>
    <p th:text="${#strings.length(fullName)}">Name length</p>
</div>
```

## Data Integration

### Working with Objects

Access nested properties easily:

```html
<!-- Object properties -->
<p>Name: [[${user.profile.displayName}]]</p>
<p>Address: [[${user.address.street}]], [[${user.address.city}]]</p>

<!-- Safe navigation (null-safe) -->
<p>Phone: [[${user.contact?.phone ?: 'Not provided'}]]</p>
```

### Collection Processing

Handle lists and arrays:

```html
<!-- Check if collection has items -->
<div th:if="${not #lists.isEmpty(products)}">
    <h3>Available Products</h3>
    <ul>
        <li th:each="product : ${products}" 
            th:text="${product.name + ' - $' + product.price}">
            Product info
        </li>
    </ul>
</div>

<!-- Filter collections -->
<div th:each="product : ${products}" th:if="${product.inStock}">
    <span th:text="${product.name}">Product</span>
</div>
```

## Best Practices

### Performance Tips

1. **Use `th:text` for simple text**: Faster than inline expressions
2. **Minimize complex expressions**: Move complex logic to data preparation
3. **Cache template compilation**: TFPP handles this automatically
4. **Use fragments for repeated content**: Reduces template size

### Security Considerations

1. **Escape user input**: Thymeleaf escapes by default
2. **Use `th:utext` carefully**: Only for trusted HTML content
3. **Validate data types**: Ensure data matches expected types
4. **Sanitize file paths**: When working with dynamic includes

### Maintainability

1. **Use meaningful variable names**: Make templates self-documenting
2. **Comment complex logic**: Explain non-obvious expressions
3. **Organize fragments**: Group related fragments together
4. **Keep templates focused**: One main purpose per template

## Common Patterns

### Dynamic CSS Classes

```html
<div th:class="${user.active} ? 'user-active' : 'user-inactive'">
    User status content
</div>

<!-- Multiple classes -->
<div th:class="'base-class ' + ${user.role} + (${user.verified} ? ' verified' : '')">
    User content
</div>
```

### Form Processing

```html
<form th:action="@{/submit}" method="post">
    <input type="text" th:value="${form.name}" name="name" />
    <input type="email" th:value="${form.email}" name="email" />
    
    <!-- Error handling -->
    <div th:if="${errors.hasFieldErrors('name')}" 
         th:errors="*{name}" class="error">
        Name error message
    </div>
</form>
```

### Configuration Templates

```yaml
server:
  port: [[${server.port ?: 8080}]]
  host: [[${server.host ?: 'localhost'}]]
  
database:
  url: [[${database.url}]]
  username: [[${database.username}]]
  # Password handled securely elsewhere
```