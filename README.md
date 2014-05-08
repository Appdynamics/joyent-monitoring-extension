# Joyent Monitoring Extension  

##Use Case

The Joyent Monitoring Extension collects the stats from Joyent cloud provider and reports them to the AppDynamics Controller.

This extension works only with the standalone machine agent.

##Installation
1. Run 'mvn clean install' from the joyent-monitoring-extension directory
2. Download the file JoyentMonitor.zip found in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file and cd into JoyentMonitor
4. Open the monitor.xml file and provide values for identity, joyent_private_key and joyent_key_name
5. Restart the Machine Agent.
6. In the AppDynamics controller, look for events in Custom Metrics|Joyent|

##Directory Structure

<table><tbody>
<tr>
<th align="left"> Directory/File </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> src/main/java </td>
<td class='confluenceTd'> Contains source code to Joyent Monitoring Extension  </td>
</tr>
<tr>
<td class='confluenceTd'> src/main/resources </td>
<td class='confluenceTd'> Contains monitor.xml </td>
</tr>
<tr>
<td class='confluenceTd'> target </td>
<td class='confluenceTd'> Only obtained when using maven. Run 'maven clean install' to get the distributable .zip file </td>
</tr>
<tr>
<td class='confluenceTd'> pom.xml </td>
<td class='confluenceTd'> Maven script file (required only if changing Java code) </td>
</tr>
</tbody>
</table>

##XML Examples

###  monitor.xml


| Param | Description |
| ----- | ----- |
| identity | User name  |
| joyent_private_key | Joyent private key |
| joyent_key_name | Key name which you have given in the joyent account. You can find this in Account Summary -> SSH after logging in to Joyent |

~~~~
<monitor>
        <name>JoyentMonitor</name>
        <type>managed</type>
        <description>Joyent monitor</description>
        <monitor-configuration></monitor-configuration>
        <monitor-run-task>
                <execution-style>periodic</execution-style>
                <execution-frequency-in-seconds>60</execution-frequency-in-seconds>
                <name>Joyent Monitor Run Task</name>
                <display-name>Joyent Monitor Task</display-name>
                <description>Joyent Monitor Task</description>
                <type>java</type>
                <execution-timeout-in-secs>60</execution-timeout-in-secs>
                <task-arguments>
                    <argument name="identity" is-required="true" default-value="abc@appdynamics.com" />
                    <argument name="joyent_private_key" is-required="true" default-value="/home/satish/AppDynamics/Joyent/appd-joyent_id_rsa" />
                    <argument name="joyent_key_name" is-required="true" default-value="test" />
	       </task-arguments>
                <java-task>
                    <classpath>joyent-monitoring-extension.jar</classpath>
                    <impl-class>com.appdynamics.monitors.joyent.JoyentMonitor</impl-class>
                </java-task>
        </monitor-run-task>
</monitor>
~~~~

##Metrics

###Instances
Metrics related to instances

| Name | Description |
| ----- | ----- |
| Custom Metrics/Joyent/Instances/{Zone}/{Instance ID}/Disk | Disk size  |
| Custom Metrics/Joyent/Instances/{Zone}/{Instance ID}/Memory | Memory size  |
| Custom Metrics/Joyent/Instances/{Zone}/{Instance ID}/State | State of the machine  |

Possible states

| State | Desc |
|----- | ----- |
| 0 | Provisioning |
| 1 | failed |
| 2 | Running |
| 3 | Stopping |
| 4 | Stopped |
| 5 | Deleted |
| 6 | offline |
| 7 | Undefined |


###Instrumentation
Metrics related to instrumentation. To see instrumentation metrics user should create instrumentations in Joyent portal.

| Name | Description |
| ----- | ----- |
| Custom Metrics/Joyent/Instrumentation/{Module}/{Stat}/{Zone}/{UUID} | Value of the instrumentation stat  |

Module : Name of the module <br>
Stat : Name of the stat <br>
Zone : Zone in which the machine resides <br>
UUID : UUID of the machine <br>

##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/joyent-monitoring-extension).

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).

