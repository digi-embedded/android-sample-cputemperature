CPU Temperature Sample Application
==================================

This example demonstrates the usage of the CPU Temperature API. Users can
configure the CPU Temperature update interval and subscribe/unsubscribe to the
temperature update service. Static temperature limit values are also displayed.

Demo requirements
-----------------

To run this example you need:

* One compatible device to host the application.
* Network connection between the device and the host PC in order to transfer 
  and launch the application.
* Establish remote target connection to your Digi hardware before running this 
  application.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The device is powered on.
2. The device is connected directly to the PC or to the Local Area Network (LAN)
   by the Ethernet cable.

Demo run
--------

The example is already configured, so all you need to do is to build and launch 
the project.

Once application starts, configure the update temperature interval and 
subscribe/unsubscribe to the temperature update service.

While subscribed, the current temperature value is updated in a time basis using
the configured time interval.

Static temperature limit values are also displayed. You can get more information
on what do they mean by clicking on the question mark button near them.

Tested on
---------

* ConnectCore 6 SBC
* ConnectCore 6 SBC v2