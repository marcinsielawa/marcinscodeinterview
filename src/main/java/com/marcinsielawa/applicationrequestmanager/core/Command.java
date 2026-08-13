package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface Command {
    
   sealed interface Targetted extends Command permits Delete, Reject, Accept, Verify, Publish {
       String id();
   }
    
   public record Create (String name, String body)   implements Command {}
   public record Delete (String id  , String reason) implements Targetted {}
   public record Reject (String id  , String reason) implements Targetted {}
   
   public record Accept (String id) implements Targetted {}
   public record Verify (String id) implements Targetted {}
   public record Publish(String id) implements Targetted {}
}
