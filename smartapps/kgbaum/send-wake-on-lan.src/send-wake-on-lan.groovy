/**
 *  Send Wake-On-Lan
 *
 *  Copyright 2018 Karl Baum
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	This smart app adds a virtual switch that sends a Wake-On-Lan packet when pushed.  Note that the uninstall does
 *	not work as expected, first delete the virtual switch that was added, then uninstall the app.
 */
definition(
    name: "Send Wake-On-Lan",
    namespace: "kgbaum",
    author: "Karl Baum",
    description: "Adds a virtual push button that sends a Wake-On-Lan packet.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
}


preferences {
	section("Button Name:") {
    	input "buttonName", "text", title: "Enter a friendly name", required: true
    }
	section("Device MAC Address:") {
		input "mac", "text", title: "MAC Address without :", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    def buttonLabel = "${settings.buttonName}"
    def deviceId = "SWON_${app.id}"
    log.debug "child momentary button tile -  Label:${buttonLabel}, Name :${deviceId}"
    
    def newDevice = getChildDevice(deviceId)
    if (!newDevice) {
    	log.debug "child does not exist, creating"
		newDevice = addChildDevice("smartthings", "Momentary Button Tile", deviceId, null, [name:deviceId, label:buttonLabel, completedSetup: true])
    }
    else {
    	log.debug "child exists, skipping creation"
    }

    subscribe(newDevice, "momentary.pushed", switchPushHandler)
}

def uninstalled() {
	log.debug "uninstalled called"
    // Removal of child device does not seem to work.  It must be manually uninstalled.
    //log.debug "unsubscribing from device events"
    //unsubscribe()
    //log.debug "deleting children"
    //getChildDevices().each {
    //   runIn(10, deleteChildDevice(it.deviceNetworkId))
    //}
}

def switchPushHandler(evt) {
	log.debug "switchOnHandler called: $evt"
    sendHubCommand(myWOLCommand())
}

def myWOLCommand() {
    def result = new physicalgraph.device.HubAction (
        "wake on lan " + mac,
        physicalgraph.device.Protocol.LAN,
        null
        //[secureCode: "111122223333"]
    )
    return result
}