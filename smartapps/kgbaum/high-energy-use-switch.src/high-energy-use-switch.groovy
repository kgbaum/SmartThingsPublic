/**
 *  Energy Use Change Alexa Notifier
 *
 *  Copyright 2019 Karl Baum
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
 */
definition(
    name: "High Energy Use Switch",
    namespace: "kgbaum",
    author: "Karl Baum",
    description: "Turns on a simulated switch when energy use is above a threshold and off when it is below.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	section ("Energy Meter to Monitor:") {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "aboveThreshold", type: "number", title: "Reports Above...", required: true, description: "in either watts or kw.")
	}
    section("Simulated Switch Sensor Name:") {
    	input "simulatedSwitchName", "text", title: "Enter a friendly name", required: true
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
    def simulatedSwitchLabel = "${settings.simulatedSwitchName}"
    state.deviceId = "SWON_${app.id}"
    log.debug "child simulated switch -  Label:${simulatedSwitchName}, Name :${state.deviceId}"
    
    def newDevice = getChildDevice(state.deviceId)
    if (!newDevice) {
    	log.debug "child does not exist, creating"
		newDevice = addChildDevice("smartthings/testing", "Simulated Switch" , state.deviceId, null, [name:state.deviceId, label:simulatedSwitchName, completedSetup: true])
        log.debug "child creation finished"
    }
    else {
    	log.debug "child exists, skipping creation"
    }
    
	subscribe(meter, "power", meterHandler)
}


def meterHandler(evt) {
    def meterValue = evt.value as double
    if (!atomicState.lastValue) {
    	atomicState.lastValue = meterValue
    }

    def lastValue = atomicState.lastValue as double
    atomicState.lastValue = meterValue

    def aboveThresholdValue = aboveThreshold as int
    if (meterValue >= aboveThresholdValue) {
    	if (lastValue < aboveThresholdValue) { // only send notifications when crossing the threshold
    	    turnSwitchOn()
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${aboveThreshold}) has already been crossed"
        }
    }

    if (meterValue < aboveThresholdValue) {
    	if (lastValue >= aboveThresholdValue) { // only send notifications when crossing the threshold
    	    turnSwitchOff()
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${belowThreshold}) has already been crossed"
        }
    }
}

def turnSwitchOn() {
	log.debug "turning on virtual switch"
    def device = getChildDevice(state.deviceId)
    if (device) {
    	device.on()
    } else {
    	log.debug "error: unable to find virtual swith"
    }
}

def turnSwitchOff() {
	log.debug "turning ff virtual switch"
    def device = getChildDevice(state.deviceId)
    if (device) {
    	device.off()
    } else {
    	log.debug "error: unable to find virtual switch"
    }
}