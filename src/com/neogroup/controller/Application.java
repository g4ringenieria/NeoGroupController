
package com.neogroup.controller;

import com.neogroup.controller.processors.ScriptsProcessor;
import com.neogroup.controller.processors.ConnectionsProcessor;
import com.neogroup.controller.processors.DeviceProcessor;
import com.neogroup.controller.processors.GeneralProcessor;
import com.neogroup.controller.processors.Processor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Application 
{
    private static final Application instance = new Application ();
    private ConnectionManager connectionManager; 
    private ConsoleManager consoleManager;
    private ScriptsManager commandManager;
    private Logger logger;
    private String name;
    private List<Processor> processors;
    
    public static Application getInstance ()
    {
        return instance;
    }
    
    public static void main(final String[] args)
    {
        try
        {
            int port = Integer.parseInt(args[0]);
            getInstance().setName(args[1]);
            getInstance().getConnectionManager().setPort(port);
            getInstance().addProcessor(new GeneralProcessor());
            getInstance().addProcessor(new ConnectionsProcessor());
            getInstance().addProcessor(new ScriptsProcessor());
            getInstance().addProcessor(new DeviceProcessor());
            getInstance().start();  
        }
        catch (Exception ex)
        {
            System.out.println ("Error: " + ex.toString() + "\nUSAGE: java -jar Controller.jar PORT ACTIONNAME");
            System.exit(1);
        }
    }
    
    private Application ()
    {
        commandManager = new ScriptsManager();
        connectionManager = new ConnectionManager();
        consoleManager = new ConsoleManager();
        processors = new ArrayList<Processor>();
    }

    public void destroy ()
    {
        stop();
        if (processors != null)
        {
            for (Processor handler : processors)
                try { handler.stop(); } catch (Exception ex) {}
            processors.clear();
            processors = null;
        }
        if (consoleManager != null)
        {
            try { consoleManager.stop(); } catch (Exception ex) {}
            consoleManager = null;
        }
        if (connectionManager != null)
        {
            try { connectionManager.stop(); } catch (Exception ex) {}
            connectionManager = null;
        }
        System.exit(0);
    }
    
    public void start ()
    {
        getLogger().info("Initializing Controller ...");
        for (Processor processor : processors)
            processor.start();
        consoleManager.start();
        connectionManager.start();
        getLogger().info("Controller initialized !!");
    }
    
    public void stop ()
    {
        getLogger().info("Finalizing Controller ...");
        consoleManager.stop();
        connectionManager.stop();
        for (Processor processor : processors)
            processor.stop();
        getLogger().info("Controller finalized !!");
    }
    
    public String getName() 
    {
        return name;
    }

    public void setName(String actionName)
    {
        this.name = actionName;
    }
    
    public ConnectionManager getConnectionManager ()
    {
        return connectionManager;
    }
    
    public ConsoleManager getConsoleManager ()
    {
        return consoleManager;
    }

    public ScriptsManager getScriptsManager() 
    {
        return commandManager;
    }
    
    public void addProcessor (Processor processor)
    {
        this.processors.add (processor);
    }
    
    public List<Processor> getProcessors ()
    {
        return processors;
    }
    
    public Logger getLogger ()
    {   
        if (logger == null)
        {
            logger = Logger.getLogger(this.getClass().getName());
            try
            {   
                FileHandler handler = new FileHandler("./logs/" + getName() + ".txt", 1024000, 1, true);
                handler.setFormatter(new Formatter() 
                {       
                    private final Date date = new Date();
                    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    @Override
                    public String format(LogRecord record) 
                    {
                        date.setTime(record.getMillis());
                        String message = formatMessage(record);
                        String throwable = "";
                        if (record.getThrown() != null) 
                        {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            pw.println();
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            throwable = sw.toString();
                        }
                        return String.format("[%s] %s: %s %s\n", dateFormatter.format(date), record.getLevel().getLocalizedName(), message, throwable);
                    }
                });
                logger.setUseParentHandlers(false);
                logger.addHandler(handler);
            } 
            catch (Exception e){e.printStackTrace();}
        }
        return logger;
    }
}