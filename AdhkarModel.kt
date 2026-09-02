package com.example.data.adhkar

data class AdhkarItem(
    val id: String,
    val number: Int,
    val title: String,
    val arabicText: String,
    val translation: String,
    val benefit: String,
    val targetCount: Int,
    val isMorning: Boolean = true,
    val isEvening: Boolean = true
)

object AdhkarRepository {

    val sabahAdhkar = listOf(
        AdhkarItem(
            id = "sabah_1",
            number = 1,
            title = "Ayat-ul-Kursi",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep...",
            benefit = "Protection for the day against all harm",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_2_ikhlas",
            number = 2,
            title = "Surah Al-Ikhlas",
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
            translation = "Say: He is Allah, [who is] One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            benefit = "Full Surah Al-Ikhlas - Equivalent to one-third of the Quran",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_2_falaq",
            number = 3,
            title = "Surah Al-Falaq",
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            translation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created, from the evil of darkness when it settles, from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            benefit = "Full Surah Al-Falaq - Protection from evil eye, magic, and envy",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_2_nas",
            number = 4,
            title = "Surah An-Nas",
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            translation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the breasts of mankind - from among the jinn and mankind.",
            benefit = "Full Surah An-Nas - Protection from whispers of Satan and evil humans/jinn",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_3",
            number = 5,
            title = "La ilaha illallah wahdahu la sharika lah...",
            arabicText = "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translation = "None has the right to be worshipped except Allah alone, with no partner. To Him belongs all sovereignty and praise, and He is over all things omnipotent.",
            benefit = "Declaration of Faith - Equivalent to freeing slaves & forgiveness of sins",
            targetCount = 10,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_4",
            number = 6,
            title = "Bismillahilladhi la yadurru...",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            translation = "In the Name of Allah, with Whose Name nothing on the earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
            benefit = "Protection from harm and calamities",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_5",
            number = 7,
            title = "Allahumma 'Afini fi badani...",
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ",
            translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight. There is no deity except You.",
            benefit = "Prayer for health, well-being and preservation of faculties",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_6",
            number = 8,
            title = "Subhanallahi wa bihamdihi 'adada khalqihi...",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ وَرِضَا نَفْسِهِ وَزِنَةَ عَرْشِهِ وَمِدَادَ كَلِمَاتِهِ",
            translation = "Glory be to Allah and all praise is His, according to the number of His creation, according to His pleasure, according to the weight of His Throne, and according to the ink of His words.",
            benefit = "Weighty words of immense reward and remembrance",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_7",
            number = 9,
            title = "Asbahna 'ala fitratil Islam...",
            arabicText = "أَصْبَحْنَا عَلَى فِطْرَةِ الْإِسْلَامِ وَعَلَى كَلِمَةِ الْإِخْلَاصِ وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            translation = "We have entered the morning upon the natural religion of Islam, the statement of sincere devotion, the religion of our Prophet Muhammad (peace be upon him)...",
            benefit = "Affirming the fitrah of Islam and the pure creed",
            targetCount = 1,
            isMorning = true,
            isEvening = false
        ),
        AdhkarItem(
            id = "sabah_8",
            number = 10,
            title = "Asbahna wa asbahal mulku lillah...",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلّٰهِ وَالْحَمْدُ لِلّٰهِ، لَا إِلٰهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translation = "We have entered the morning and the sovereignty belongs to Allah, all praise is due to Allah. None has the right to be worshipped except Allah alone...",
            benefit = "Morning belongs to Allah - Gratitude for life and sustenance",
            targetCount = 1,
            isMorning = true,
            isEvening = false
        ),
        AdhkarItem(
            id = "sabah_9",
            number = 11,
            title = "Allahumma bika asbahna...",
            arabicText = "اللّٰهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
            translation = "O Allah, by You we have reached the morning, and by You we reach the evening, by You we live, by You we die, and unto You is the resurrection.",
            benefit = "By You we reach morning - Entrusting existence to Allah",
            targetCount = 1,
            isMorning = true,
            isEvening = false
        ),
        AdhkarItem(
            id = "sabah_10",
            number = 12,
            title = "Allahumma inni asbahtu ushhiduka...",
            arabicText = "اللّٰهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللّٰهُ لَا إِلٰهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            translation = "O Allah, this morning I call upon You, the carriers of Your Throne, Your angels and all of Your creation to witness that You are Allah, there is no deity except You...",
            benefit = "Bearing witness to Allah's oneness - Protection from hellfire",
            targetCount = 4,
            isMorning = true,
            isEvening = false
        ),
        AdhkarItem(
            id = "sabah_11",
            number = 13,
            title = "Allahumma inni as'alukal 'afwa...",
            arabicText = "اللّٰهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللّٰهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللّٰهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي",
            translation = "O Allah, I ask You for pardon and well-being in this world and the Next. O Allah, I ask You for pardon and well-being in my religion, worldly affairs, family and wealth...",
            benefit = "Comprehensive supplication for protection and well-being",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_12",
            number = 14,
            title = "Sayyid al-Istighfar",
            arabicText = "اللّٰهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            translation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can...",
            benefit = "Master of seeking forgiveness - Entrance to Jannah upon dying that day",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_13",
            number = 15,
            title = "Allahumma 'Alimal ghaibi...",
            arabicText = "اللّٰهُمَّ عَالِمَ الْغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ",
            translation = "O Allah, Knower of the unseen and the witnessed, Originator of the heavens and the earth, Lord and Sovereign of all things, I testify that there is no deity except You...",
            benefit = "Protection from evil desires of the soul and the devil",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_14",
            number = 16,
            title = "Hasbiyallahu la ilaha illa huwa...",
            arabicText = "حَسْبِيَ اللّٰهُ لَا إِلٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            translation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            benefit = "Allah is sufficient for all worldly and religious worries",
            targetCount = 7,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_15",
            number = 17,
            title = "Ya Hayyu Ya Qayyum...",
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            translation = "O Ever-Living, O Sustainer, by Your mercy I seek assistance; rectify for me all of my affairs and do not leave me to myself even for the blink of an eye.",
            benefit = "Calling upon the Great Names of Allah for divine aid",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_16",
            number = 18,
            title = "Raditu billahi Rabba...",
            arabicText = "رَضِيتُ بِاللّٰهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace be upon him) as my Prophet.",
            benefit = "Allah's pleasure is guaranteed for whoever recites this",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_17",
            number = 19,
            title = "Allahumma salli 'ala Muhammadin...",
            arabicText = "اللّٰهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            translation = "O Allah, bestow Your blessings upon Muhammad and the family of Muhammad, as You bestowed blessings upon Ibrahim and the family of Ibrahim...",
            benefit = "Sending blessings on the Prophet ﷺ - Intercession on the Day of Judgment",
            targetCount = 10,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_18",
            number = 20,
            title = "Astaghfirullaha wa atubu ilayh",
            arabicText = "أَسْتَغْفِرُ اللّٰهَ وَأَتُوبُ إِلَيْهِ",
            translation = "I seek the forgiveness of Allah and repent to Him.",
            benefit = "Seeking forgiveness - Sins erased and abundance granted",
            targetCount = 100,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sabah_19",
            number = 21,
            title = "Subhanallahi wa bihamdihi",
            arabicText = "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ",
            translation = "Glory be to Allah and His is the praise.",
            benefit = "Glorifying Allah - Sins forgiven even if like the foam of the sea",
            targetCount = 100,
            isMorning = true,
            isEvening = true
        )
    )

    val shamAdhkar = listOf(
        AdhkarItem(
            id = "sham_1",
            number = 1,
            title = "Ayat-ul-Kursi",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence...",
            benefit = "Protection for the evening against all evil",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_2_ikhlas",
            number = 2,
            title = "Surah Al-Ikhlas",
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
            translation = "Say: He is Allah, [who is] One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            benefit = "Full Surah Al-Ikhlas - Protection from all evil during the night",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_2_falaq",
            number = 3,
            title = "Surah Al-Falaq",
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            translation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created, from the evil of darkness when it settles, from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            benefit = "Full Surah Al-Falaq - Protection against evil eye, magic, and dark harms",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_2_nas",
            number = 4,
            title = "Surah An-Nas",
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            translation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the breasts of mankind - from among the jinn and mankind.",
            benefit = "Full Surah An-Nas - Protection from satanic whispers through the night",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_3",
            number = 5,
            title = "La ilaha illallah wahdahu la sharika lah...",
            arabicText = "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translation = "None has the right to be worshipped except Allah alone, with no partner...",
            benefit = "Declaration of Faith - Heavy in the scales on the Day of Judgment",
            targetCount = 10,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_4",
            number = 6,
            title = "Bismillahilladhi la yadurru...",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            translation = "In the Name of Allah, with Whose Name nothing on the earth or in the heavens can cause harm...",
            benefit = "Protection from unexpected harm and disasters through the night",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_5",
            number = 7,
            title = "Allahumma 'Afini fi badani...",
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ",
            translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight...",
            benefit = "Prayer for physical and spiritual health and protection",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_6",
            number = 8,
            title = "Subhanallahi wa bihamdihi 'adada khalqihi...",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ وَرِضَا نَفْسِهِ وَزِنَةَ عَرْشِهِ وَمِدَادَ كَلِمَاتِهِ",
            translation = "Glory be to Allah and all praise is His, according to the number of His creation...",
            benefit = "Weighty words of magnificent praise and boundless reward",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_7",
            number = 9,
            title = "Amsayna 'ala fitratil Islam...",
            arabicText = "أَمْسَيْنَا عَلَى فِطْرَةِ الْإِسْلَامِ وَعَلَى كَلِمَةِ الْإِخْلَاصِ وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            translation = "We have entered the evening upon the natural religion of Islam, the statement of sincere devotion, the religion of our Prophet Muhammad (peace be upon him)...",
            benefit = "Affirming devotion to the pure religion of Islam at sunset",
            targetCount = 1,
            isMorning = false,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_8",
            number = 10,
            title = "Amsayna wa amsal mulku lillah...",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translation = "We have entered the evening and the sovereignty belongs to Allah, all praise is due to Allah. None has the right to be worshipped except Allah alone...",
            benefit = "Evening belongs to Allah - Complete reliance on Allah",
            targetCount = 1,
            isMorning = false,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_9",
            number = 11,
            title = "Allahumma bika amsayna...",
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
            translation = "O Allah, by You we have reached the evening, and by You we reach the morning, by You we live, by You we die, and unto You is our final return.",
            benefit = "By You we reach evening - Acknowledging Allah as the source of all life",
            targetCount = 1,
            isMorning = false,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_10",
            number = 12,
            title = "Allahumma inni amsaytu ushhiduka...",
            arabicText = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            translation = "O Allah, this evening I call upon You, the carriers of Your Throne, Your angels and all of Your creation to witness that You are Allah...",
            benefit = "Bearing witness to Allah's oneness at night",
            targetCount = 4,
            isMorning = false,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_11",
            number = 13,
            title = "Allahumma inni as'alukal 'afwa...",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي",
            translation = "O Allah, I ask You for pardon and well-being in this world and the Next...",
            benefit = "Seeking divine protection and well-being through the night",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_12",
            number = 14,
            title = "Sayyid al-Istighfar",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            translation = "O Allah, You are my Lord, none has the right to be worshipped except You...",
            benefit = "Master of seeking forgiveness - Entrance to Jannah upon dying that night",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_13",
            number = 15,
            title = "Allahumma 'Alimal ghaibi...",
            arabicText = "اللَّهُمَّ عَالِمَ الْغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ",
            translation = "O Allah, Knower of the unseen and the witnessed, Originator of the heavens and the earth...",
            benefit = "Protection against evil influences and whispers of the night",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_14",
            number = 16,
            title = "Hasbiyallahu la ilaha illa huwa...",
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            translation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            benefit = "Allah is sufficient for all worries and difficulties",
            targetCount = 7,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_15",
            number = 17,
            title = "Ya Hayyu Ya Qayyum...",
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            translation = "O Ever-Living, O Sustainer, by Your mercy I seek assistance...",
            benefit = "Supplicating for guidance and protection in all affairs",
            targetCount = 1,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_16",
            number = 18,
            title = "Raditu billahi Rabba...",
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace be upon him) as my Prophet.",
            benefit = "Divine contentment and eternal reward guaranteed",
            targetCount = 3,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_17",
            number = 19,
            title = "Allahumma salli 'ala Muhammadin...",
            arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            translation = "O Allah, bestow Your blessings upon Muhammad and the family of Muhammad...",
            benefit = "Sending blessings upon the beloved Messenger of Allah ﷺ",
            targetCount = 10,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_18",
            number = 20,
            title = "Astaghfirullaha wa atubu ilayh",
            arabicText = "أَسْتَغْفِرُ اللّٰهَ وَأَتُوبُ إِلَيْهِ",
            translation = "I seek the forgiveness of Allah and repent to Him.",
            benefit = "Seeking sincere forgiveness before sleep",
            targetCount = 100,
            isMorning = true,
            isEvening = true
        ),
        AdhkarItem(
            id = "sham_19",
            number = 21,
            title = "Subhanallahi wa bihamdihi",
            arabicText = "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ",
            translation = "Glory be to Allah and His is the praise.",
            benefit = "Glorification of Allah yielding vast rewards in the hereafter",
            targetCount = 100,
            isMorning = true,
            isEvening = true
        )
    )

    fun getAll(): List<AdhkarItem> = sabahAdhkar + shamAdhkar

    fun findById(id: String): AdhkarItem? {
        return sabahAdhkar.find { it.id == id } ?: shamAdhkar.find { it.id == id }
    }
}
