import {useEffect,useMemo,useState} from 'react';
import {Alert,AppBar,Box,Button,Card,CardContent,Chip,Collapse,Container,CssBaseline,FormControl,Grid,IconButton,InputLabel,MenuItem,Select,Stack,Table,TableBody,TableCell,TableContainer,TableHead,TableRow,Toolbar,Typography,createTheme,ThemeProvider} from '@mui/material';
import {DarkMode,LightMode,FileDownload,ExpandMore,OpenInNew} from '@mui/icons-material';
import {Line} from 'react-chartjs-2';
import {Chart as ChartJS,CategoryScale,LinearScale,PointElement,LineElement,Tooltip,Legend,Filler} from 'chart.js';
import {getBoxBenchmarks,getPrices} from './api';
import type {BoxBenchmark,Price} from './types';

ChartJS.register(CategoryScale,LinearScale,PointElement,LineElement,Tooltip,Legend,Filler);
const juiceLabels:Record<string,string>={FCOJ:'Suco concentrado e congelado (FCOJ)',NFC:'Suco não concentrado (NFC)'};

function App(){
 const [dark,setDark]=useState(true);
 const [product,setProduct]=useState('FCOJ');
 const [prices,setPrices]=useState<Price[]>([]);
 const [benchmarks,setBenchmarks]=useState<BoxBenchmark[]>([]);
 const [showCepea,setShowCepea]=useState(false);
 const [error,setError]=useState('');
 const theme=useMemo(()=>createTheme({palette:{mode:dark?'dark':'light',primary:{main:'#ff8a1f'},background:{default:dark?'#0c1117':'#f5f7fa',paper:dark?'#151d27':'#fff'}},shape:{borderRadius:14},typography:{fontFamily:'Inter,system-ui,sans-serif',h4:{fontWeight:800},h5:{fontWeight:800}}}),[dark]);

 useEffect(()=>{getPrices({country:'CN',size:500}).then(p=>{setPrices(p.content);setError('')}).catch(()=>setError('Dados indisponíveis no momento. Tente novamente em instantes.'))},[]);
 useEffect(()=>{getBoxBenchmarks().then(setBenchmarks).catch(()=>setBenchmarks([]))},[]);

 const juicePrices=prices.filter(p=>p.product===product);
 const latestFcoj=prices.find(p=>p.product==='FCOJ');
 const latestNfc=prices.find(p=>p.product==='NFC');
 const latestDate=[latestFcoj?.referenceDate,latestNfc?.referenceDate].filter(Boolean).sort().at(-1)??'—';
 const chart=[...juicePrices].reverse();
 const data={labels:chart.map(p=>p.referenceDate),datasets:[{label:'USD/kg',data:chart.map(p=>p.priceUsd),borderColor:'#ff8a1f',backgroundColor:'#ff8a1f22',fill:true,tension:.25}]};

 function csv(){
  const rows=[['date','market','product','cny_tonne','usd_kg','brl_kg','source','confidence'],...juicePrices.map(p=>[p.referenceDate,p.market,p.product,p.originalPrice,p.priceUsd,p.priceBrl,p.source,p.confidenceScore])];
  const blob=new Blob([rows.map(r=>r.map(x=>`"${String(x).replaceAll('"','""')}"`).join(',')).join('\n')],{type:'text/csv'});
  const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=`omip-${product.toLowerCase()}.csv`;a.click();URL.revokeObjectURL(a.href);
 }

 const JuiceKpi=({label,price}: {label:string;price?:Price})=><Card sx={{height:'100%'}}><CardContent><Typography color="text.secondary" variant="body2">{label}</Typography><Typography variant="h5" mt={1}>{price?`¥ ${price.originalPrice.toLocaleString('pt-BR',{minimumFractionDigits:2})}/t`:'—'}</Typography><Typography variant="caption" color="text.secondary">{price?`US$ ${price.priceUsd.toFixed(2)}/kg · ${price.referenceDate}`:'Sem cotação'}</Typography></CardContent></Card>;

 return <ThemeProvider theme={theme}><CssBaseline/>
  <AppBar position="sticky" color="transparent" elevation={0} sx={{backdropFilter:'blur(16px)',borderBottom:1,borderColor:'divider'}}><Toolbar><Box component="span" role="img" aria-label="Laranja" sx={{fontSize:24,lineHeight:1,mr:1.25,filter:'drop-shadow(0 2px 4px rgba(255,138,31,.28))'}}>🍊</Box><Typography fontWeight={800} sx={{flexGrow:1}}>OMIP <Typography component="span" color="text.secondary">Orange Intelligence</Typography></Typography><IconButton onClick={()=>setDark(v=>!v)} aria-label="Alternar tema">{dark?<LightMode/>:<DarkMode/>}</IconButton></Toolbar></AppBar>
  <Container maxWidth="xl" sx={{py:4}}>
   <Stack spacing={1} mb={4}><Typography variant="overline" color="primary">China-first market monitoring</Typography><Typography variant="h4">Inteligência de mercado da laranja</Typography><Typography color="text.secondary">Fruta e suco analisados separadamente, com unidades, fontes e níveis de mercado rastreáveis.</Typography></Stack>
   {error&&<Alert severity="warning" sx={{mb:3}}>{error}</Alert>}

   <Box component="section" mb={5}>
    <Stack direction={{xs:'column',sm:'row'}} justifyContent="space-between" alignItems={{sm:'center'}} spacing={1} mb={2}><Box><Typography variant="overline" color="primary">Fruta</Typography><Typography variant="h5">Mercado da fruta 🍊</Typography><Typography variant="body2" color="text.secondary">Preços por caixa de 40,8 kg. Não incluem preços de suco.</Typography></Box><Chip label="Unidade: R$/caixa 40,8 kg" variant="outlined" color="primary"/></Stack>
    <Grid container spacing={2} mb={2}>{benchmarks.map(b=><Grid size={{xs:12,md:6}} key={b.country}><Card sx={{height:'100%'}}><CardContent><Stack direction="row" justifyContent="space-between" alignItems="flex-start"><Box><Typography color="text.secondary" variant="body2">{b.label}</Typography><Typography variant="h4" fontWeight={800} mt={.5}>{b.priceBoxBrl.toLocaleString('pt-BR',{style:'currency',currency:'BRL'})}</Typography></Box><Chip label={b.country==='BR'?'Brasil':'China'} color={b.country==='CN'?'primary':'success'} variant="outlined"/></Stack><Typography variant="body2" mt={1.5}>{b.product}</Typography><Typography variant="caption" color="text.secondary" display="block">{b.marketLevel} · {b.referencePeriod}</Typography><Button component="a" href={b.url} target="_blank" rel="noreferrer" size="small" sx={{mt:.75,p:0,minWidth:0,textTransform:'none'}}>Fonte: {b.source}</Button></CardContent></Card></Grid>)}</Grid>
    <Card variant="outlined"><CardContent sx={{pb:'16px!important'}}><Stack direction={{xs:'column',sm:'row'}} alignItems={{sm:'center'}} justifyContent="space-between" spacing={2}><Box><Typography fontWeight={700}>Cotações oficiais CEPEA</Typography><Typography variant="body2" color="text.secondary">Tabela regional de frutas; não contém preços chineses de suco.</Typography></Box><Stack direction="row" spacing={1}><Button size="small" endIcon={<ExpandMore sx={{transform:showCepea?'rotate(180deg)':'none',transition:'transform .2s'}}/>} onClick={()=>setShowCepea(v=>!v)} aria-expanded={showCepea}>{showCepea?'Recolher painel':'Exibir painel'}</Button><Button component="a" href="https://www.hfbrasil.org.br/br/estatistica/citros.aspx" target="_blank" rel="noreferrer" size="small" endIcon={<OpenInNew/>}>Abrir fonte</Button></Stack></Stack><Collapse in={showCepea} unmountOnExit><Box component="iframe" title="Cotações oficiais de citros — Hortifruti Brasil/CEPEA" src="https://www.hfbrasil.org.br/br/estatistica/citros.aspx" loading="lazy" sx={{width:'100%',height:{xs:560,md:680},border:0,borderRadius:2,mt:2,backgroundColor:'#fff'}}/></Collapse></CardContent></Card>
   </Box>

   <Box component="section">
    <Stack direction={{xs:'column',sm:'row'}} justifyContent="space-between" alignItems={{sm:'center'}} spacing={1} mb={2}><Box><Typography variant="overline" color="primary">Suco</Typography><Typography variant="h5">Mercado de suco 🧃</Typography><Typography variant="body2" color="text.secondary">FCOJ e NFC são produtos distintos e nunca são combinados em uma única média.</Typography></Box><Chip label="Mercado: China" color="primary" variant="outlined"/></Stack>
    <Grid container spacing={2} mb={3}><Grid size={{xs:12,sm:6,md:3}}><JuiceKpi label="FCOJ atual" price={latestFcoj}/></Grid><Grid size={{xs:12,sm:6,md:3}}><JuiceKpi label="NFC atual" price={latestNfc}/></Grid><Grid size={{xs:12,sm:6,md:3}}><Card sx={{height:'100%'}}><CardContent><Typography color="text.secondary" variant="body2">Última referência</Typography><Typography variant="h5" mt={1}>{latestDate}</Typography><Typography variant="caption" color="text.secondary">Boletim chinês mais recente</Typography></CardContent></Card></Grid><Grid size={{xs:12,sm:6,md:3}}><Card sx={{height:'100%'}}><CardContent><Typography color="text.secondary" variant="body2">Confiabilidade</Typography><Typography variant="h5" mt={1}>{latestFcoj?.confidenceScore??latestNfc?.confidenceScore??'—'}/100</Typography><Typography variant="caption" color="text.secondary">Fonte publicada; sem consenso independente</Typography></CardContent></Card></Grid></Grid>
    <Stack direction={{xs:'column',sm:'row'}} spacing={2} mb={3}><FormControl sx={{minWidth:300}}><InputLabel>Tipo de suco</InputLabel><Select value={product} label="Tipo de suco" onChange={e=>setProduct(e.target.value)}>{Object.entries(juiceLabels).map(([value,label])=><MenuItem value={value} key={value}>{label}</MenuItem>)}</Select></FormControl><Button startIcon={<FileDownload/>} onClick={csv} disabled={!juicePrices.length}>Exportar {product}</Button></Stack>
    <Grid container spacing={3}><Grid size={{xs:12,lg:7}}><Card><CardContent><Typography variant="h6" mb={.5}>Histórico — {juiceLabels[product]}</Typography><Typography variant="caption" color="text.secondary" display="block" mb={2}>Valores normalizados em USD por kg</Typography><Box height={360}><Line data={data} options={{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}}}}/></Box></CardContent></Card></Grid><Grid size={{xs:12,lg:5}}><Card><CardContent><Typography variant="h6" mb={2}>Cotações — {product}</Typography><TableContainer sx={{maxHeight:390}}><Table stickyHeader size="small"><TableHead><TableRow><TableCell>Data</TableCell><TableCell align="right">CNY/t</TableCell><TableCell align="right">USD/kg</TableCell><TableCell>Fonte</TableCell></TableRow></TableHead><TableBody>{juicePrices.map(p=><TableRow key={p.id}><TableCell>{p.referenceDate}</TableCell><TableCell align="right">{p.originalPrice.toLocaleString('pt-BR',{minimumFractionDigits:2})}</TableCell><TableCell align="right">{p.priceUsd.toFixed(2)}</TableCell><TableCell><Typography variant="caption">{p.source}</Typography></TableCell></TableRow>)}</TableBody></Table></TableContainer></CardContent></Card></Grid></Grid>
   </Box>

   <Typography variant="caption" color="text.secondary" display="block" mt={3}>Preços de fruta e suco não são diretamente comparáveis. Consulte sempre produto, unidade, nível de mercado, fonte e data antes de decisões comerciais.</Typography>
   <Box component="footer" sx={{mt:4,pt:2.5,borderTop:1,borderColor:'divider',textAlign:'center'}}><Typography variant="caption" color="text.secondary" display="block">© 2026 OMIP — Todos os direitos reservados. By — Lindberg de Oliveira.</Typography><Typography component="a" href="https://creativecommons.org/licenses/by-nc/4.0/deed.pt-br" target="_blank" rel="license noreferrer" variant="caption" color="text.secondary" sx={{display:'inline-block',mt:.5}}>Dados CEPEA/ESALQ-USP — CC BY-NC 4.0 — somente uso não comercial</Typography></Box>
  </Container>
 </ThemeProvider>;
}

export default App;
