package net.fagnersales;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import org.java_websocket.client.WebSocketClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;

class DataCoords {
	double x;
	double y;
	double z;

	public DataCoords(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}

class DataHead {
	float yaw;
	double y;

	public DataHead(float yaw, double y) {
		this.yaw = yaw;
		this.y = y;
	}
}


class Data {
	DataCoords coords;
	DataHead head;
	float movement_speed;
}


public class CoordinatorModClient implements ClientModInitializer {
	private WebSocketClient webSocketClient;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_WORLD_TICK.register((world) -> {
			if (webSocketClient == null) {
				initializeWebsocket();
			} else if (webSocketClient.isClosed()) {
				initializeWebsocket();
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register((minecraftClient) -> {
			if (minecraftClient.world == null || !minecraftClient.world.isClient()) {
				return;
			}

			PlayerEntity player = minecraftClient.player;

			if (player != null && webSocketClient != null && webSocketClient.isOpen()) {
				Data data = new Data();
				data.coords = new DataCoords(
						player.getX(),
						player.getY(),
						player.getZ()
				);
				data.head = new DataHead(player.getHeadYaw(), player.getPitch());
				data.movement_speed = player.getMovementSpeed();

				Gson gson = new Gson();
				String jsonData = gson.toJson(data);

				webSocketClient.send(jsonData);
			}
		});
	}

	public void initializeWebsocket() {
		try {
			webSocketClient = new CoordinatorWSClient(new URI("ws://localhost:8080/ws"));
			webSocketClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}