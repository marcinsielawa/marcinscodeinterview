package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface Command {
    
   sealed interface Targetted extends Command permits Delete {
       String id();
   }
    
   public record Create(String name, String body)   implements Command {}
   public record Delete(String id  , String reason) implements Targetted {}
}
