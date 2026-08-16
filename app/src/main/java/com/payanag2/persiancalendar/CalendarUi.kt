package com.payanag2.persiancalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate

private val months = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
private val week = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

@Composable
fun PersianCalendarApp() {
    val today = LocalDate.now()
    val tj = remember(today) { g2j(today) }
    var year by remember { mutableIntStateOf(tj[0]) }
    var month by remember { mutableIntStateOf(tj[1]) }
    var selected by remember { mutableIntStateOf(tj[2]) }
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("تقویم فارسی", fontWeight = FontWeight.Bold) }, actions = { TextButton({ year=tj[0]; month=tj[1]; selected=tj[2] }) { Text("امروز") } }) },
        bottomBar = { NavigationBar {
            NavigationBarItem(tab == 0, { tab=0 }, icon={Text("▦")}, label={Text("تقویم")})
            NavigationBarItem(tab == 1, { tab=1 }, icon={Text("☷")}, label={Text("رویدادها")})
            NavigationBarItem(tab == 2, { tab=2 }, icon={Text("⚙")}, label={Text("تنظیمات")})
        }}
    ) { p ->
        when(tab) {
            0 -> CalendarPage(Modifier.padding(p), year, month, selected, tj, { if(month==1){month=12;year--}else month--;selected=1 }, { if(month==12){month=1;year++}else month++;selected=1 }, {selected=it})
            1 -> EventsPage(Modifier.padding(p), year, month, selected)
            else -> SettingsPage(Modifier.padding(p))
        }
    }
}

@Composable private fun CalendarPage(mod: Modifier, y:Int, m:Int, sel:Int, today:IntArray, prev:()->Unit, next:()->Unit, choose:(Int)->Unit) {
    val count = if(m<=6) 31 else if(m<=11) 30 else if(leap(y)) 30 else 29
    val first = LocalDate.of(*j2g(y,m,1)).dayOfWeek
    val offset = when(first){DayOfWeek.SATURDAY->0;DayOfWeek.SUNDAY->1;DayOfWeek.MONDAY->2;DayOfWeek.TUESDAY->3;DayOfWeek.WEDNESDAY->4;DayOfWeek.THURSDAY->5;DayOfWeek.FRIDAY->6}
    LazyColumn(mod.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Card(Modifier.fillMaxWidth(), RoundedCornerShape(28.dp), colors=CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(20.dp)) { Text("امروز"); Text("${pd(today[2])} ${months[today[1]-1]} ${pd(today[0])}", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold); Text(dayName(today)) } } }
        item { Card(shape=RoundedCornerShape(24.dp)) { Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) { Text("‹", Modifier.size(48.dp).clickable{prev()}, fontSize=34.sp, textAlign=TextAlign.Center); Column(Modifier.weight(1f), horizontalAlignment=Alignment.CenterHorizontally){Text(months[m-1],style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(pd(y))};Text("›",Modifier.size(48.dp).clickable{next()},fontSize=34.sp,textAlign=TextAlign.Center) }
            Row(Modifier.fillMaxWidth()){week.forEach{Text(it,Modifier.weight(1f),textAlign=TextAlign.Center,fontWeight=FontWeight.Bold)}}
            repeat((offset+count+6)/7){r->Row(Modifier.fillMaxWidth()){repeat(7){c->val n=r*7+c-offset+1;if(n in 1..count) Day(n,n==today[2]&&m==today[1]&&y==today[0],n==sel,choose) else Spacer(Modifier.weight(1f).height(52.dp))}}}
        } } }
        item { Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp)){Text("جزئیات روز",fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("${dayName(j2gAsJ(y,m,sel))}، ${pd(sel)} ${months[m-1]}");Spacer(Modifier.height(8.dp));Text("رویدادی برای این روز ثبت نشده است.",color=MaterialTheme.colorScheme.onSurfaceVariant)}} }
    }
}

@Composable private fun Day(n:Int,today:Boolean,selected:Boolean,on:(Int)->Unit){Box(Modifier.weight(1f).height(52.dp).padding(3.dp).clip(CircleShape).background(if(selected)MaterialTheme.colorScheme.primary else Color.Transparent).clickable{on(n)},contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(pd(n),color=if(selected)MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,fontWeight=if(today||selected)FontWeight.Bold else FontWeight.Normal);if(today&&!selected)Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))}}}

@Composable private fun EventsPage(mod:Modifier,y:Int,m:Int,d:Int){Column(mod.fillMaxSize().padding(20.dp)){Text("رویدادها",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("${pd(d)} ${months[m-1]} ${pd(y)}",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(18.dp));Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(20.dp)){Text("مناسبت‌های روز",fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text("مناسبتی برای این روز ثبت نشده است.",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}

@Composable private fun SettingsPage(mod:Modifier){Column(mod.fillMaxSize().padding(20.dp)){Text("تنظیمات",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(16.dp));Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("ظاهر و نمایش",fontWeight=FontWeight.Bold);Text("اعداد فارسی و رابط راست‌به‌چپ");Text("نمایش تقویم جلالی و روز هفته");Text("نسخه بعدی: مناسبت‌ها، یادآورها، ویجت و حالت تاریک")}}}}

private fun pd(n:Int)=n.toString().map{if(it in '0'..'9')('۰'.code+it-'0').toChar()else it}.joinToString("")
private fun g2j(d:LocalDate):IntArray=g2j(d.year,d.monthValue,d.dayOfMonth)
private fun g2j(gy:Int,gm:Int,gd:Int):IntArray{val md=intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334);var jy=979;val y=gy-1600;val y2=if(gm>2)y+1 else y;var days=365*y+(y2+3)/4-(y2+99)/100+(y2+399)/400-80+gd+md[gm-1];jy+=33*(days/12053);days%=12053;jy+=4*(days/1461);days%=1461;if(days>365){jy+=(days-1)/365;days=(days-1)%365};return intArrayOf(jy,if(days<186)1+days/31 else 7+(days-186)/30,1+if(days<186)days%31 else(days-186)%30)}
private fun j2g(jy:Int,jm:Int,jd:Int):IntArray{val y=jy-979;val days=365*y+(y/33)*8+(y%33+3)/4+78+jd+if(jm<7)(jm-1)*31 else(jm-1)*30+6;var gy=1600+400*(days/146097);var d=days%146097;var lp=true;if(d>=36525){d--;gy+=100*(d/36524);d%=36524;if(d>=365) d++ else lp=false};gy+=4*(d/1461);d%=1461;if(d>=366){lp=false;d--;gy+=d/365;d%=365};val ml=intArrayOf(31,if(lp)29 else 28,31,30,31,30,31,31,30,31,30,31);var gm=1;var rem=d+1;while(rem>ml[gm-1]){rem-=ml[gm-1];gm++};return intArrayOf(gy,gm,rem)}
private fun j2gAsJ(y:Int,m:Int,d:Int)=g2j(*j2g(y,m,d))
private fun dayName(j:IntArray):String=when(LocalDate.of(*j2g(j[0],j[1],j[2])).dayOfWeek){DayOfWeek.SATURDAY->"شنبه";DayOfWeek.SUNDAY->"یکشنبه";DayOfWeek.MONDAY->"دوشنبه";DayOfWeek.TUESDAY->"سه‌شنبه";DayOfWeek.WEDNESDAY->"چهارشنبه";DayOfWeek.THURSDAY->"پنجشنبه";DayOfWeek.FRIDAY->"جمعه"}
private fun leap(y:Int):Boolean=try{LocalDate.of(*j2g(y,12,30));true}catch(_:Exception){false}
