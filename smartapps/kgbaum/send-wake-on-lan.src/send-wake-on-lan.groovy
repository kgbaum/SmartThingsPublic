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
 *	This smart app sends a Wake-On-Lan packet when the associated switch is turned on.  A virtual device switch
 *	can first be created for use with the app.
 */
definition(
    name: "Send Wake-On-Lan",
    namespace: "kgbaum",
    author: "Karl Baum",
    description: "Turns on computer using Wake-On-Lan",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
}


preferences {
	section("Switch used for turning on PC:") {
    	input "theSwitch", "capability.switch", required: true
    }
	section("PC MAC Address:") {
		input "mac", "text", required: true
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
	subscribe(theSwitch, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
	log.debug "switchOnHandler called: $evt"
    sendHubCommand(myWOLCommand())
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