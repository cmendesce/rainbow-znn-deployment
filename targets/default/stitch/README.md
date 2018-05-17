Extension:  Stitch scripts must have the extension ".s"; otherwise, the
Adaptation Manager will not know to load it.

# DoS Self-Protection approach using Rainbow

To improve a system’s security, it is first necessary to develop a repertoire of countermeasures that can be applied in to the system.

## Tatics

Tactics in Rainbow are specified through the Stitch adaptation
language. Tactics require three parts to be specified: (1) the
condition, which specifies when a tactic is applicable; (2) the action,
which defines the script for making changes (to the model of
) the system; and (3) the effect, which specifies the expected effect
that the tactic will have on the model. In keeping with closed-loop
control conventions, when a tactic is executed in Rainbow, changes
are not made directly to the model. Rainbow translates these operations
into effectors that execute on the system. Gauges then update
the model according to the changes they observe.

These tatics are defined to 

#### Adding Capacity

This tactic commissions a new replicated web
server. An equal portion of all requests will be sent to all active
servers. To integrate this into Rainbow, we need to know how many
servers are active and how many may be added. In the model of the
system, we separate the components into those that are active in
Znn and those that are available resources in the environment.

#### Reducing Service

Znn has three fidelity levels: high, which includes
full multimedia content and retrieves information from the
database; medium, which has low resolution images; and text only,
which does not provide any multimedia content. This tactic reduces
the level of service one step (e.g, from high to medium). The
fidelity level is represented in the architecture model by annotating
servers with a fidelity property.

#### Blackholing

If a (set of) IPs is determined to be attacking the system,
then we use this tactic to add the IP address to the blacklist of
Znn. In the model, we need to know two things: (1) what are the
currently blacklisted clients, and (2) which clients are candidates
for blacklisting. In the architectural model, each load balancer
component defines a property that reflects the currently blacklisted
IPs, and each client in the model has a property that indicates the
probability that it is malicious.

#### Throttling

Znn has the capability to limit the rate of requests accepted
from certain IPs. In the model, these IPs are stored in a
property of each load balancer representing the clients that are being
throttled in this way. Similar to Blackholing, the maliciousness
property on client components in the model can be used to indicate
potential candidates.

#### Captcha 

Znn can dynamically enable and disable Captcha, by forwarding
requests to a Captcha processor. Captcha acts as a Turing
test, verifying that the requester is human.

#### Reauthenticate

Znn has a public interface and a private interface
for subscribing clients. This tactic closes the public interface
and forces subscribing clients to re-authenticate with Znn. Like
Captcha, Reauthentication verifies whether the requester is a human.
However, re-authentication is more strict than Captcha because
it requires that the requester be registered with the system.
After re-authentication is deployed, anonymous users will be cut
off from the system.


Source: [Architecture-Based Self-Protection: Composing and
Reasoning about Denial-of-Service Mitigations](http://www.cs.cmu.edu/~jcmoreno/files/hotsos14.pdf)
