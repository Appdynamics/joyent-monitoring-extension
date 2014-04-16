# Joyent Monitoring Extension  

##Use Case

The Joyent Monitoring Extension collects the stats from Joyent cloud provider and reports them on the Controller.

This extension works only with the standalone machine agent.

##Installation
1. Run 'mvn clean install' from the joyent-monitoring-extension directory
2. Download the file JoyentMonitor.zip found in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file and cd into JoyentMonitor
4. Open the monitor.xml file and provide values for identity, joyent_private_key, joyent_key_name and instrumentation_file_path.
5. Open the instrumentations.xml file and mark enabled to true for the instrumentations you want to run. By default only 10 instrumentations are allowed.
6. If your account is capable of creating more than 10 instrumentations, set that value to max_instrumentations_to_run in monitor.xml. 
7. Restart the Machine Agent.
8. In the AppDynamics controller, look for events in Custom Metrics|Joyent|

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
<td class='confluenceTd'> Contains monitor.xml and instrumentations.xml </td>
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
| instrumentation_file_path | Full path to the instrumentations.xml file |
| max_instrumentations_to_run | Number of instrumentations to run. Joyent allowes maximum of 10 instrumentations by default |

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
                    <argument name="instrumentation_file_path" is-required="false" default-value="instrumentations.xml" />
                    <!--Joyent by default supports only 10 instrumentations. If your account has more, specify that number here -->
                    <argument name="max_instrumentations_to_run" is-required="false" default-value="10" />
		       </task-arguments>
                <java-task>
                    <classpath>joyent-monitoring-extension.jar;lib/bcprov-jdk15-140.jar;lib/jackson-annotations-2.1.5.jar;lib/jackson-core-2.1.5.jar;lib/jackson-databind-2.1.5.jar;lib/xmlpull-1.1.3.1.jar;lib/xpp3_min-1.1.4c.jar;lib/xstream-1.4.7.jar;</classpath>
                    <impl-class>com.appdynamics.monitors.joyent.JoyentMonitor</impl-class>
                </java-task>
        </monitor-run-task>
</monitor>
~~~~

###instrumentations.xml

<b>All the instrumentations in instrumentations.xml are defined by Joyent. For more info visit http://apidocs.joyent.com/cloudapi/#DescribeAnalytics 

| Param | Description |
| ---- | ---- |
| \<module\> | Defines a module |
| \<stat\>  | Defines an instrumentation |
| \<name\>  | Name of the instrumentation |
| \<label\>  | Display name of the instrumentation |

~~~~
    <instrumentations>
        <module name="apache">
            <stat enabled="true">
                <name>httpd_ops</name>
                <label>HTTP requests</label>
            </stat>
        </module>
        <module name="cpu">
            <stat enabled="false">
                <name>thread_samples</name>
                <label>thread samples</label>
            </stat>
            <stat enabled="false">
                <name>thread_executions</name>
                <label>thread executions</label>
            </stat>
            <stat enabled="false">
                <name>usage</name>
                <label>aggregated CPU usage</label>
            </stat>
            <stat enabled="false">
                <name>waittime</name>
                <label>aggregated wait time</label>
            </stat>
        </module>
        ---
        ---
        ---
    </instrumentations> 
~~~~

##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/joyent-monitoring-extension).

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).

