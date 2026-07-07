package se.w3footprint.friluft.domain.model

data class City(val name: String, val lat: Double, val lon: Double)

val SWEDISH_CITIES = listOf(
    City("Stockholm", 59.3293, 18.0686),
    City("Göteborg", 57.7089, 11.9746),
    City("Malmö", 55.6050, 13.0038),
    City("Uppsala", 59.8586, 17.6389),
    City("Västerås", 59.6099, 16.5448),
    City("Örebro", 59.2753, 15.2134),
    City("Linköping", 58.4108, 15.6214),
    City("Helsingborg", 56.0465, 12.6945),
    City("Jönköping", 57.7826, 14.1618),
    City("Norrköping", 58.5877, 16.1924),
    City("Lund", 55.7047, 13.1910),
    City("Umeå", 63.8258, 20.2630),
    City("Gävle", 60.6749, 17.1413),
    City("Borås", 57.7210, 12.9401),
    City("Eskilstuna", 59.3666, 16.5077),
    City("Halmstad", 56.6745, 12.8578),
    City("Sundsvall", 62.3908, 17.3069),
    City("Östersund", 63.1792, 14.6357),
    City("Luleå", 65.5848, 22.1567),
    City("Karlstad", 59.3793, 13.5036),
    City("Växjö", 56.8777, 14.8091),
    City("Kalmar", 56.6634, 16.3566),
    City("Falun", 60.6065, 15.6355),
    City("Kristianstad", 56.0294, 14.1567),
    City("Karlskrona", 56.1616, 15.5866),
    City("Visby", 57.6348, 18.2948),
    City("Kiruna", 67.8557, 20.2253),
)
