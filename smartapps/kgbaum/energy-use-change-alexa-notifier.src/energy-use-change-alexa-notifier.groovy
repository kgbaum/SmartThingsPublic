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
    name: "Energy Use Change Alexa Notifier",
    namespace: "kgbaum",
    author: "Karl Baum",
    description: "Toggles a simulated contact switch when energy use drops below or raises above a threshold.  Alexa routines can trigger off the simulated contact switch for periodic notifications.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	section ("Energy Meter to Monitor:") {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "aboveThreshold", type: "number", title: "Reports Above...", required: true, description: "in either watts or kw.")
        input(name: "belowThreshold", type: "number", title: "Or Reports Below...", required: true, description: "in either watts or kw.")
	}
    section("Simulated Contact Sensor Name:") {
    	input "simulatedContactName", "text", title: "Enter a friendly name", required: true
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
    def simulatedContactLabel = "${settings.simulatedContactName}"
    state.deviceId = "SWON_${app.id}"
    log.debug "child simulated contact -  Label:${simulatedContactName}, Name :${state.deviceId}"
    
    def newDevice = getChildDevice(state.deviceId)
    if (!newDevice) {
    	log.debug "child does not exist, creating"
		newDevice = addChildDevice("smartthings/testing", "Simulated Contact Sensor" , state.deviceId, null, [name:state.deviceId, label:simulatedContactName, completedSetup: true])
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
    if (meterValue > aboveThresholdValue) {
    	if (lastValue < aboveThresholdValue) { // only send notifications when crossing the threshold
    	    openVirtual()
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${aboveThreshold}) has already been crossed"
        }
    }

    def belowThresholdValue = belowThreshold as int

    if (meterValue < belowThresholdValue) {
    	if (lastValue > belowThresholdValue) { // only send notifications when crossing the threshold
		    def msg = "${meter} reported ${evt.value} ${dUnit} which is below your threshold of ${belowThreshold}."
    	    openVirtual()
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${belowThreshold}) has already been crossed"
        }
    }
}

def openVirtual() {
	log.debug "opening virtual contact"
    def device = getChildDevice(state.deviceId)
    if (device) {
    	device.open()
    	runIn(30, closeVirtual, [overwrite: true])
    } else {
    	log.debug "error: unable to find virtual contact"
    }
}

def closeVirtual() {
	log.debug "closing virtual contact"
    def device = getChildDevice(state.deviceId)
    if (device) {
    	device.close()
    } else {
    	log.debug "error: unable to find virtual contact"
    }
}