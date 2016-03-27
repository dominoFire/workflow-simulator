package org.perez.resource;

import java.io.Serializable;
import java.io.StringReader;

public class ResourceConfig
	implements Serializable
{
	/**
	 * VM configuration name
	 */
	protected String name;

	/**
	 * Number of cores in the VM configuration
	 */
	protected int cores;

	/**
	 * Amount of RAM memory in MegaBytes in the VM configuration
	 */
	protected double ramMemory;

	/**
	 * Name of the cloud provider (Azure, AWS, Rackspace, ...) that supports this configuration
	 */
	protected String provider;

	/**
	 * The SPECfp score of this configuration
	 */
	protected double speedFactor;

	/**
	 * The cost of running this virtual machine during an hour, in US Dollars.
	 * Note that hour fractions shouldn't be charged by the cloud provider
	 */
	protected double cost;


	public ResourceConfig() {

	}

	public ResourceConfig(String name, int cores, double ramMemory, String provider, double speedFactor, double cost) {
		this.name = name;
		this.cores = cores;
		this.ramMemory = ramMemory;
		this.provider = provider;
		this.speedFactor = speedFactor;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCores() {
		return cores;
	}

	public void setCores(int cores) {
		this.cores = cores;
	}

	public double getRamMemory() {
		return ramMemory;
	}

	public void setRamMemory(double ramMemory) {
		this.ramMemory = ramMemory;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public double getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ResourceConfig))
			return false;
		ResourceConfig rc  = (ResourceConfig)o;

		return  this.name.equals(rc.name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
