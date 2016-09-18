package org.perez.workflow.elements;

import java.io.Serializable;

/**
 * Created by Fernando on 06/07/2014.
 */
public class Resource
  implements Serializable
{
    /** Name of the resource */
    protected String name;
    /** A number indicating how fast this resource is (think as 'velocity') */
    protected double speedFactor;
    /** Time when the resources is ready. Initally 0 */
    protected double readyTime;
    /** Cost per hour for executing this workflow */
    protected double costHour;

    public Resource(String name, double speedFactor) {
        this.setName(name);
        this.setSpeedFactor(speedFactor);
        this.setReadyTime(0.);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name.equals(null))
            throw new IllegalArgumentException("Resource name is null");
        if(name.equals(""))
            throw new IllegalArgumentException("Resource name is empty");
        this.name = name;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public void setSpeedFactor(double speedFactor) {
        if(speedFactor < 0)
            throw new IllegalArgumentException("Resource speed factor is less than zero");
        //see: http://stackoverflow.com/questions/3728309/difference-among-double-min-normal-and-double-min-value
        if(Math.abs(speedFactor) < 0.000000001)
            throw new IllegalArgumentException("Resource speed factor is zero");
        this.speedFactor = speedFactor;
    }

    public double getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(double readyTime) {
        if(readyTime < 0)
            throw new IllegalArgumentException("Resource ready time is less than zero");
        this.readyTime = readyTime;
    }

    public double getCostHour() {
        return costHour;
    }

    public void setCostHour(double costHour) {
        if(costHour < 0.)
            throw new IllegalArgumentException("Cost per hour cannot be null");
        this.costHour = costHour;
    }

    public void addReadyTime(double readyTime) {
        if(readyTime < 0)
            throw new IllegalArgumentException("Resource ready time is less than zero");
        this.readyTime += readyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        if (name != null ? !name.equals(resource.name) : resource.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
