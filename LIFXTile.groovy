/**
 *
 * Copyright 2018, 2019 Robert Heyes. All Rights Reserved
 *
 *  This software is free for Private Use. You may use and modify the software without distributing it.
 *  You may not grant a sublicense to modify and distribute this software to third parties.
 *  Software is provided without warranty and your use of it is at your own risk.
 *
 */

metadata {
    definition(name: 'LIFX Tile', namespace: 'robheyes', author: 'Robert Alan Heyes', importUrl: 'https://raw.githubusercontent.com/robheyes/lifxcode/master/LIFXTile.groovy') {
        capability 'Light'
        capability 'ColorControl'
        capability 'ColorTemperature'
        capability 'Polling'
        capability 'Initialize'
        capability 'Switch'

        attribute "label", "string"
        attribute "group", "string"
        attribute "location", "string"
		
		command "setTileEffect", [[name:"Tile Effect Command", type: "ENUM", description: "Pick an option", constraints: ["Off","Morph","Flame"] ] ]
    }

    preferences {
        input "useActivityLogFlag", "bool", title: "Enable activity logging", required: false
        input "useDebugActivityLogFlag", "bool", title: "Enable debug logging", required: false
        input "defaultTransition", "decimal", title: "Level transition time", description: "Set transition time (seconds)", required: true, defaultValue: 0.0
    }
}

@SuppressWarnings("unused")
def installed() {
    initialize()
}

@SuppressWarnings("unused")
def updated() {
    initialize()
}

def initialize() {
    state.transitionTime = defaultTransition
    state.useActivityLog = useActivityLogFlag
    state.useActivityLogDebug = useDebugActivityLogFlag
    unschedule()
    requestInfo()
    runEvery1Minute poll
}

@SuppressWarnings("unused")
def refresh() {

}

@SuppressWarnings("unused")
def poll() {
    parent.lifxQuery(device, 'DEVICE.GET_POWER') { List buffer -> sendPacket buffer }
    parent.lifxQuery(device, 'LIGHT.GET_STATE') { List buffer -> sendPacket buffer }
}

def requestInfo() {
    parent.lifxQuery(device, 'LIGHT.GET_STATE') { List buffer -> sendPacket buffer }
}

def on() {
    sendActions parent.deviceOnOff('on', getUseActivityLog(), state.transitionTime ?: 0)
}


def off() {
    sendActions parent.deviceOnOff('off', getUseActivityLog(), state.transitionTime ?: 0)
}

@SuppressWarnings("unused")
def setColor(Map colorMap) {

}

@SuppressWarnings("unused")
def setHue(number) {

}

@SuppressWarnings("unused")
def setSaturation(number) {

}

@SuppressWarnings("unused")
def setColorTemperature(temperature) {

}

@SuppressWarnings("unused")
def setTileEffect(effectType) {
	log.debug "TileEffect:  ${effectType}"
	sendActions parent.deviceSetTileEffect(effectType.toUpperCase(), 3, "")
}

private void sendActions(Map<String, List> actions) {
    actions.commands?.each { item -> parent.lifxCommand(device, item.cmd, item.payload) { List buffer -> sendPacket buffer, true } }
    actions.events?.each { sendEvent it }
}

def parse(String description) {
    List<Map> events = parent.parseForDevice(device, description, getUseActivityLog())
    events.collect { createEvent(it) }
}

private String myIp() {
    device.getDeviceNetworkId()
}

private void sendPacket(List buffer, boolean noResponseExpected = false) {
    String stringBytes = hubitat.helper.HexUtils.byteArrayToHexString parent.asByteArray(buffer)
    sendHubCommand(
            new hubitat.device.HubAction(
                    stringBytes,
                    hubitat.device.Protocol.LAN,
                    [
                            type              : hubitat.device.HubAction.Type.LAN_TYPE_UDPCLIENT,
                            destinationAddress: myIp() + ":56700",
                            encoding          : hubitat.device.HubAction.Encoding.HEX_STRING,
                            ignoreResponse    : noResponseExpected
                    ]
            )
    )
}

def getUseActivityLog() {
    if (state.useActivityLog == null) {
        state.useActivityLog = true
    }
    return state.useActivityLog
}

def setUseActivityLog(value) {
    log.debug("Setting useActivityLog to ${value ? 'true':'false'}")
    state.useActivityLog = value
}

def getUseActivityLogDebug() {
    if (state.useActivityLogDebug == null) {
        state.useActivityLogDebug = false
    }
    return state.useActivityLogDebug
}

def setUseActivityLogDebug(value) {
    log.debug("Setting useActivityLogDebug to ${value ? 'true':'false'}")
    state.useActivityLogDebug = value
}

void logDebug(msg) {
    if (getUseActivityLogDebug()) {
        log.debug msg
    }
}

void logInfo(msg) {
    if (getUseActivityLog()) {
        log.info msg
    }
}

void logWarn(String msg) {
    if (getUseActivityLog()) {
        log.warn msg
    }
}