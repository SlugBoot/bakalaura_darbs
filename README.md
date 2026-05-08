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

## 12. februāris

1. Izveidots Java Spring Starter projekts, jo Apache Guacamole ir pieejams API Java valodā
    - Pievienots Lombok, Thymeleaf, Spring Web, Spring DevTools, Spring DataJPA

## 16. februāris

1. Izveidots savienojums ar PostgreSQL datubāzi

2. Pievienota papildus atkarība
    - Spring Boot Starter Validation

## 25. februāris

1. Izveidots primitīvs modelis datubāzes savienojuma pārbaudei

## 3. marts

1. Veiktas izmaiņas Person klasē - skatīt iesniegumu
2. Izveidotas 2 jaunas modeļu klases:
    - Student
    - Course

## 6. marts

1. Izveidoti virspusēji repozitoriji (t.i., bez filtriem)
2. Izveidotas jaunas modeļu klases un izmaiņas esošajās
3. Pārbaudīta datu ievade PostgreSQL datubāzē

## 11. marts

1. Instalēts [Proxmox VE](docs/Proxmox.md)
2. Papildus darbības [Proxmox VE](docs/Proxmox.md#lxc-konteiners-un-ssh-atslēga)

3. Izveidots primitīvs Studentu CRUD portāla aplikācijai

## 13. marts

1. Pielabots Studentu CRUD
2. Izveidots primitīvs Profesoru CRUD protāla aplikācijai

## 12. aprīlis

1. Izveidoti daži [Ansible](/docs/Ansible/ansible.md#12-aprīlis) "playbook"

## 14. aprīlis

1. Izveidota primitīva portāla sākumlapa profesoriem
    - Izveidots kontrolieris un serviss
2. Izmaiņas modeļu klasēs, lai labāk sakristu ar vajadzībām
3. Izmaiņas aplikācijas testēšanas datu izveidē
4. Papildināts Profesoru CRUD
5. Minimālas izmaiņas modeļu klasēs
6. Iesākts CRUD kursiem
7. Papildināts kursu CRUD
8. Izveidoti papildus testa dati datubāzes pārbaudei

## 4. maijs

1. Pievienoti priekšaizstāvēšanas faili
2. Papildināts studentu CRUD
3. Papildināts profesoru CRUD
4. Papildināts kursu CRUD

## 5. maijs

1. Papildināts kursu un studentu CRUD

## 6. maijs

1. Papildināts Kursu un Studentu CRUD operācijas
2. Implementēta primitīva Spring Security konfigurācija
3. Portālu izveides sākumi

## 8. maijs

1. Kļūdas labojums profesoru CRUD kontrolierī
2. Izveidots primitīvs sistēmas serviss ar nolūku, ka tas:
    - Pārvaldīs automātiski ģenerētus Ansible failus (t.i., playbook un inventory faili)
    - Izpildīs sistēmas komandas
