package vn.edu.hust.student.haicm.cognitiveldentify;

/**
 * Created by DucPC on 3/5/2018.
 */

public class Student {
    private String name;
    private String id;

    public Student(String name, String id){
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String getId(){
        return this.id;
    }

    public void setName(String name){
        this.name = name;
    }
}
