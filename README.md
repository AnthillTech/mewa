# Specyfikacja systemu: Device Channels

## Kanały
1. Do kanału można przypiąć dowolną ilość urządzeń
1. Każde urządzenie posiada unikalną w ramach kanału nazwę za pomocą której jest identyfikowane
1. Kanał posiada API za pomocą którego można
    1. Pobrać listę podłączonych urządzeń
    1. Wysłać komendę do wybranego urządzenia
    1. Zarejestrować się na otrzymywanie eventów z wybranego urządzenia


## Urządzenia
1. Urządzenie zwraca informację o:
    1. Swojej nazwie
    1. Akceptowanych komendach
    1. Wysyłanych eventach


## Przykłady urządzeń

### Czujka na okno
#### Komendy
1. Brak
#### Eventy
1. Otwarcie okna

### Centralka
Aplikacja pracująca na telefonie, służąca do zarządzania systemem.
Pobiera listę urządzeń w kanale i pozwala wysłać do nich określone komendy lub reagować na eventy.
#### Komendy
1. Wyślij wiadomość. Jeżeli w kanale są 2 centralki to mogą dzięki tej komendzie się komunikować

### System alarmowy
Aplikacja pracująca na serwerze. Rejestruje się na eventy z czujek i wysyła własny zbiorczy event typu alarm, jeżeli jest zazbrojona
#### Komendy
1. Zazbrojenie
1. Rozbrojenie
#### Eventy
1. Alarm