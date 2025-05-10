# SSHook: Simple Screenshot Uploader via. Discord Webhooks

WARNING: This mod uses an insecure way to transmit webhooks between the server and client. There is no encryption, and any packet sniffer could access the webhook. This is by design. This mod is intended for small groups of friends who want an easy and lightweight way to upload their screenshots to various channels in Discord. The risk with leaked webhooks is unintentional messages.

SSHook is an easy way for users to upload screenshots to Discord using the webhook integration feature. Every time a user takes a screenshot, they will be prompted with a configurable list of webhooks (Discord channels) to upload the screenshot. The webhooks are synced between the client and server if the client has the 'enableServerOverride' config option set. This means that only the server owner has to define the webhooks; the mod handles syncing them. 

CLIENTS:
  All users can define client-side webhooks and use a toggle config option to display the upload prompt. If the user connects to a server with the mod installed (and enableServerOverride is set on the client), the server will send applicable webhooks to the client to use instead. By default, this option is enabled.

SERVERS:
  Servers do not need this mod for clients to upload screenshots, but each client must define the webhooks individually in the config (and therefore, manually share the webhook URLs). 
  If the mod is installed on the server, a separate config file will be created where the server owner can define webhooks to be shared with the clients.

CONFIG:
  CLIENTS: See sshook.json for details on what each option does. You can also visit the in-game configuration through the Cloth API.
  SERVERS: sshook-server.json is generated with server installations, it has 3 options:
    - enableBotNameOverride: this will replace the webhook (bot) name with minecraft usernaem of the person who uploaded, as well as their avatar.
    - webhooks: this is where you can define the name and url of the webhook.
    - customServerName: This configures the 'Server' category in the embed. If left blank, the server IP will be defaulted.
