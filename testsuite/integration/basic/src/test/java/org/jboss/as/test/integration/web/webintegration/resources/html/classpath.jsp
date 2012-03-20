<!-- Tests of JSP page class access
-->
<%@page import="javax.management.j2ee.statistics.EJBStats"%>
<%@page contentType="text/html" import="java.io.*,java.security.*,javax.naming.*,org.jboss.as.test.integration.web.webintegration.util.*"%>
<%!private String formatException(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }%>
<html>
<head>
<title>JSP Debug Page</title>
</head>
<body>
    <h2>Classpath Access Tests</h2>
    <h3>Access WEB-INF/org/jboss/as/test/integration/web/webintegration/util/ClassInClasses.class</h3>
    <pre>
<%
    // Access a class from the WEB-INF/classes directory
    try {
        Class<?> clazz = Class.forName("org.jboss.as.test.integration.web.webintegration.util.ClassInClasses");
        out.println("Successfully loaded class: " + clazz.getName());
        ClassLoader cl = clazz.getClassLoader();
        ProtectionDomain pd = clazz.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        out.println("  ClassLoader : " + cl.getClass().getName() + ':' + cl.hashCode());
        out.println("  CodeSource.location : " + cs.getLocation());
        Class<?> clazz2 = ClassInClasses.class;
        if (clazz2.equals(clazz) == false)
            throw new ServletException("ClassCastException for ClassInClasses.class");
        out.println("Static org.jboss.as.test.web.webintegration.util.ClassInClasses.class matches Class.forName");
    } catch (AccessControlException e) {
        // Ignore security manager related failures.
        e.printStackTrace();
    } catch (Exception e) {
        out.println("Failed");
        String dump = formatException(e);
        out.println(dump);
    }
%>
</pre>
    <h3>Access WEB-INF/lib/util.jar!org/jboss/as/test/integration/web/webintegration/util/Util.class</h3>
    <pre>
<%
    // Access a class from a WEB-INF/lib jar
    try {
        Class<?> clazz = Class.forName("org.jboss.as.test.integration.web.webintegration.util.Util");
        out.println("Successfully loaded class: " + clazz.getName());
        ClassLoader cl = clazz.getClassLoader();
        ProtectionDomain pd = clazz.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        out.println("  ClassLoader : " + cl.getClass().getName() + ':' + cl.hashCode());
        out.println("  CodeSource.location : " + cs.getLocation());
        Class<?> clazz2 = Util.class;
        if (clazz2.equals(clazz) == false)
            throw new ServletException("ClassCastException for Util.class");
        out.println("Static org.jboss.as.test.web.webintegration.util.Util.class matches Class.forName");
    } catch (AccessControlException e) {
        // Ignore security manager related failures.
        e.printStackTrace();
    } catch (Exception e) {
        out.println("Failed");
        String dump = formatException(e);
        out.println(dump);
    }
%>
</pre>
    <h3>Access javax.management.j2ee.EJBStats from server/default/lib</h3>
    <pre>
<%
    // Access a class from the server config lib directory
    try {
        Class<?> clazz = Class.forName("javax.management.j2ee.EJBStats");
        out.println("Successfully loaded class: " + clazz.getName());
        ClassLoader cl = clazz.getClassLoader();
        ProtectionDomain pd = clazz.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        out.println("  ClassLoader : " + cl.getClass().getName() + ':' + cl.hashCode());
        out.println("  CodeSource.location : " + cs.getLocation());
        Class<?> clazz2 = EJBStats.class;
        if (clazz2.equals(clazz) == false)
            throw new ServletException("ClassCastException for EJBStats.class");
        out.println("Static javax.management.j2ee.EJBStats.class matches Class.forName");
    } catch (AccessControlException e) {
        // Ignore security manager related failures.
        e.printStackTrace();
    } catch (Exception e) {
        out.println("Failed");
        String dump = formatException(e);
        out.println(dump);
    }
%>
</pre>
    <jsp:useBean id="util" scope="session" class="org.jboss.as.test.integration.web.webintegration.util.Util" />
    <h2>JSP ClassLoaders</h2>
    <pre>
<%
    try {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String loaders = Util.displayClassLoaders(loader);
        out.println(loaders);
    } catch (AccessControlException e) {
        // Ignore security manager related failures.
        e.printStackTrace();
    } catch (Exception e) {
        throw new ServletException(e);
    }
%>
</pre>
    <h2>JSP View of JNDI ENC</h2>
    <pre>
<%
    try {
        String enc = Util.displayENC();
        out.println(enc);
    } catch (NamingException e) {
        throw new ServletException(e.toString(true), e);
    } catch (Exception e) {
        throw new ServletException(e);
    }
%>
</pre>
</body>
</html>