# Specification

## Introduction

Mewa is channel server which allows secure communication between different devices (physical or virtual).
Devices can join channel and communicate only with other devices which are connected to the same channel.
Channels are protected with passwords.

## Model

data Mewa = Mewa [Channel]
data Channel = Channel Name Password [Device]
data Device = Device Name

### Accessing server state information

* Get names of all connected to the channel devices.


## Functionality (With state changes)

* Podłączenie urządzenia do kanału z nową nazwą urządzenia

* Podłączenie urządzenia do kanału z istniejącą nazwą urządzenie

* Odłączenie urządzenia od kanału

* Wysłanie eventu

* Wysłanie message


## Laws

* Na podstawie eventów o dołączeniu i rozłączeniu, można zawsze sprawdzić ilość aktualnie podłączonych urządzeń.
