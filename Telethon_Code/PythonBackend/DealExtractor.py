# +
from telethon import TelegramClient, events

import Telethon

import asyncio
# -

async def main():
    session_string,api_id,api_hash,channel = Telethon.read_config()
    client = await Telethon.create_client(session_string,api_id,api_hash)

    print("Keeping Cloud up-to-date.")
    print()
    await Telethon.update_messages_to_cloud()

    await client.start()

    @client.on(events.NewMessage(chats = [channel]))
    async def handler(event):
        print("New Message Received.")
        print()
        try:
            await Telethon.update_messages_to_cloud()
            print("Message has been updated into Cloud.")
            print()
        except:
            print("Unable to update into Cloud.")
            print()
            
    print("Extractor is ready.")
    print()    
    
    await client.run_until_disconnected()

if __name__ == "__main__":
    asyncio.run(main())
    # or await main() if using jupyter notebook


