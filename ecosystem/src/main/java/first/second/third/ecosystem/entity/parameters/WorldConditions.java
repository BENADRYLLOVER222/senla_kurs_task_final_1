package first.second.third.ecosystem.entity.parameters;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorldConditions {
    private int year;
    private int temperature;
    private int accessibleWater;
    private int humidityPercentage;

    private static WorldConditions instance;

    public static WorldConditions getInstance() {
        if (instance == null) {
            instance = new WorldConditions();
        }
        return instance;
    }


}
