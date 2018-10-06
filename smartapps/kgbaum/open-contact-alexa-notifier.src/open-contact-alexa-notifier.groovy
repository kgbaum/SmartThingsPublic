/**
 *  Open Contact Alexa Notifier
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
 */
definition(
    name: "Open Contact Alexa Notifier",
    namespace: "kgbaum",
    author: "Karl Baum",
    description: "Toggles a simulated contact switch periodically as long as a physical contact switch is open.  Alexa routines can trigger off the simulated contact switch for periodic notifications.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
/* Based off of https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/smartapps/smartthings/left-it-open.src/left-it-open.groovy */

preferences {
    section("Contact Sensor To Monitor:") {
		input "contactSensor", "capability.contactSensor", required: true
	}
    
	section("Simulated Contact Sensor Name:") {
    	input "simulatedContactName", "text", title: "Enter a friendly name", required: true
    }
    
    section("Minutes between notifications (default 10):") {
		input "notificationFrequency", "number", required: false
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
	log.debug "initialized"
    state.freqInMin = (notificationFrequency != null && notificationFrequency != "") ? notificationFrequency : 10
    
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
    
   	subscribe(contactSensor, "contact.open", contactSensorOpened)
    subscribe(contactSensor, "contact.closed", contactSensorClosed)
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

def contactSensorOpened(evt) {
	log.debug "contactSensorOpened called: $evt"
  	runIn(state.freqInMin*60, contactOpenTooLong, [overwrite: true])
}

def contactSensorClosed(evt) {
	log.debug "contactSensorClosed called: $evt"
    unschedule(contactOpenTooLong)
}

def contactOpenTooLong() {
  def contactState = contactSensor.currentState("contact")

  if (contactState.value == "open") {
    def elapsed = now() - contactState.rawDateCreated.time
    def threshold = state.freqInMin*60*1000-1000
    if (elapsed >= threshold) {
      log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling notify()"
      openVirtual()
      runIn(state.freqInMin*60, contactOpenTooLong, [overwrite: false])
    } else {
      log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
    }
  } else {
    log.warn "contactOpenTooLong() called but contact is closed:  doing nothing"
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

