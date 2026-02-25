# Virtuālās laboratorijas izveide ar tīmekļa tehnoloģijām
Development of a virtual laboratory using web technologies


# Bakalaura darba mērķis
Izveidot mācību platformu, kas pieejama tīmekļa pārlūkprogrammā. 
Tajā būs iespējams apgūt un pilnveidot zināšanas par Linux sistēmu un citiem rīkiem virtuālā laboratorijas vidē.
Pasniedzējs pārvalda un novērtē dažādus uzdevumus izmantojot virtuālas ierīces, students var šos uzdevumus izpildīt sev parocīgā vietā un vidē.

# Bakalaura darba uzdevumi
1. Izpētīt pieejamās tīmekļa un izstrādes tehnoloģijas ar kurām varētu pilnveidot virtuālās laboratorijas izveidi.
2. Izveidot virtuālo laboratoriju izveides sistēmu, lai varētu ātri un parocīgi izveidot dažāda veida un izmēra laboratorijas.
3. Izveidot pasniedzējiem portālu, kas ir atverams tīmekļa pārlūkprogrammā, kurā varētu izveidot, dzēst un citādā veidā pārvaldīt virtuālās ierīces laboratorijas vidē. Šajā portālā pasniedzējs var arī novērtēt studentu darbus, kā arī atvērt jaunus darbus studentiem.
4. Izveidot studentu portālu, kas ir atverams tīmekļa pārlūkprogrammā, kur var apskatīt atvērtos un izpildītos uzdevumus, to termiņus un izpildīto uzdevumu rezultātus. Šajā portālā var arī izvēlēties nākamo uzdevumu, kuru students vēlas pildīt.
5. Izveidot autorizēšanās sistēmu, lai var ar lietotāja kontu pieslēgties portālam. Pēc autorizēšanās lietotājam tiks piešķirtas attiecīgas tiesības veikt darbības sistēmā.
6. Automatizēt laboratorijas vižu izveidi un pārraudzību.


# Pagaidām izdarītais Git Repozitorijā

1. Izveidots Java Spring Starter projekts, jo Apache Guacamole ir pieejams API Java valodā
    - Pievienots Lombok, Thymeleaf, Spring Web, Spring DevTools, Spring DataJPA

2. Izveidots savienojums ar PostgreSQL datubāzi

3. Pievienota papildus atkarība
    - Spring Boot Starter Validation

4. Izveidots primitīvs modelis datubāzes savienojuma pārbaudei
