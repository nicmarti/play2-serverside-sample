play2-serverside-sample
=======================

This is a play2 sample to demonstrate SSE (Server Sent events) with Play2.

I really encourage you to have a look at [EventSource specification on W3.org](http://dev.w3.org/html5/eventsource/) which describe EventSource.
Using this API rather than emulating it using XMLHttpRequest or an iframe allows the user agent to make better
use of network resources in cases where the user agent implementor and the network operator are able
to coordinate in advance. Amongst other benefits, this can result in significant savings in battery life
on portable devices. This is discussed further in the section below on connectionless push.

The application has been tested successfully with Play 2.0.3-RC2, with Safari and Chrome.

For more details about Server sent events, read this blog article :
http://www.html5rocks.com/en/tutorials/eventsource/basics/

Thanks to Sadek Drobi (CTO at [Zenexity](http://www.zenexity.com/) for its help. This sample is similar to https://gist.github.com/3072893.

EventSource is supported by all modern browser except Microsoft Internet Explorer. See [this Matrix](http://caniuse.com/#feat=eventsource) for more details.

Nicolas
@nmartignole
