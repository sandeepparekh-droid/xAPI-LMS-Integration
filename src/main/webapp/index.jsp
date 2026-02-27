<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>xAPI LMS Integration</title>
    <style>
        /* WCAG 2.2 focus indicator (2.4.7 Focus Visible, 2.4.11 Focus Not Obscured) */
        *:focus {
            outline: 3px solid #005a9c;
            outline-offset: 2px;
        }
        body {
            font-family: Arial, Helvetica, sans-serif;
            margin: 0;
            padding: 0;
            color: #1a1a1a;
            background-color: #ffffff;
            line-height: 1.5;
        }
        /* WCAG 2.4.1 Bypass Blocks - skip link */
        .skip-link {
            position: absolute;
            left: -9999px;
            top: auto;
            width: 1px;
            height: 1px;
            overflow: hidden;
            z-index: 9999;
            padding: 0.5rem 1rem;
            background: #ffffff;
            color: #005a9c;
            font-size: 1rem;
            text-decoration: underline;
        }
        .skip-link:focus {
            position: static;
            width: auto;
            height: auto;
        }
        header {
            background-color: #003366;
            color: #ffffff;
            padding: 1rem 2rem;
        }
        header h1 {
            margin: 0;
            font-size: 1.5rem;
        }
        main {
            max-width: 48rem;
            margin: 2rem auto;
            padding: 0 1rem;
        }
        h2 {
            color: #003366;
        }
        p {
            font-size: 1rem;
        }
        a {
            color: #005a9c;
            text-decoration: underline;
        }
        a:hover, a:focus {
            color: #003366;
        }
        footer {
            border-top: 1px solid #cccccc;
            padding: 1rem 2rem;
            text-align: center;
            font-size: 0.875rem;
            color: #555555;
        }
    </style>
</head>
<body>
    <a class="skip-link" href="#main-content">Skip to main content</a>

    <header role="banner">
        <h1>xAPI LMS Integration</h1>
    </header>

    <nav role="navigation" aria-label="Main navigation">
        <ul style="list-style:none;padding:0.5rem 2rem;margin:0;background:#f5f5f5;">
            <li style="display:inline;margin-right:1rem;">
                <a href="<%= request.getContextPath() %>/">Home</a>
            </li>
        </ul>
    </nav>

    <main id="main-content" role="main">
        <h2>Welcome</h2>
        <p>
            This application provides an xAPI proxy for Learning Management System
            integration. It enables LTI-based single sign-on and relays xAPI
            statements to a configured Learning Record Store (LRS).
        </p>
        <h2>Endpoints</h2>
        <table role="table" aria-label="Available API endpoints">
            <caption>Available xAPI Proxy Endpoints</caption>
            <thead>
                <tr>
                    <th scope="col">Path</th>
                    <th scope="col">Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><code>/sso</code></td>
                    <td>LTI Single Sign-On endpoint</td>
                </tr>
                <tr>
                    <td><code>/xapi/*</code></td>
                    <td>xAPI proxy relay to the configured LRS</td>
                </tr>
            </tbody>
        </table>
    </main>

    <footer role="contentinfo">
        <p>&copy; Pearson Education, Inc. All rights reserved.</p>
    </footer>
</body>
</html>
