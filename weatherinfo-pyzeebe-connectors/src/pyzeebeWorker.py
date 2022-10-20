import asyncio
from pyzeebe import ZeebeWorker, create_camunda_cloud_channel
from activities import rainActivities, sunnyActivities

# main function called by the program
async def main():
    # create a channel to Camunda 8 SaaS
    channel = create_camunda_cloud_channel(
            client_id= "u2Ss0Xzidrjt~FZExzBYvoB5loL50LnJ",
            client_secret="b.aQM5OF40HqpD1sf80h_gDfGmnUo1356Ua_2hKWwqnd405-J5xpp5scCFzNqx5u",
            cluster_id= "8f2a53cb-87a7-4992-a17b-940625fa3882",
            region="bru-2",
            ) # connect to your cloud instance
    
     # Create a worker object to register your workers
    worker = ZeebeWorker(channel)

    # Create a worker for task type "activity-lookup"
    @worker.task(task_type="activitiy-lookup")
    def lookup_activities(location, weather) -> dict: # parameters are matched to process variables
      print(f"The weather at {location} is {weather}")
      if weather == "Rain" or weather == "Snow":
        return {"activity": rainActivities[location]}
      return {"activity": sunnyActivities[location]}
    
    await worker.work() # start the workers


asyncio.run(main())