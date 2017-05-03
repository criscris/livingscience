package utils.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public abstract class JettyServer 
{
	Server server;
	
	public JettyServer(int port)
	{
		switchOffJettyLogging();
		
		server = new Server(port);
		
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		init(context);
	}
	
	public static void switchOffJettyLogging()
	{
		final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("org.eclipse.jetty");
//		if (!(logger instanceof ch.qos.logback.classic.Logger)) {
//		    return;
//		}
		//ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
		//logbackLogger.setLevel(ch.qos.logback.classic.Level.WARN);
	}
	
	public void exec() throws Exception
	{
		server.start();
		
	    Runtime.getRuntime().addShutdownHook(new Thread()
	    {
	        @Override
	        public void run()
	        {
	        	JettyServer.this.stop();
	        }
	    } );
		
	    System.out.println("Server started at " + server.getURI());
	    server.join();
	}
	
	public void stop()
	{
    	try 
    	{
			if (!server.isStopped()) server.stop();
		} 
    	catch (Exception e) 
		{
			e.printStackTrace();
		}
    	System.out.println("Server shutdown.");
	}
	
	
	public abstract void init(ServletContextHandler context);
}
